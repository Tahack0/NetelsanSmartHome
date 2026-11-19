const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const bodyParser = require('body-parser');

// --- ŞEMALAR ---
const ModuleSchema = new mongoose.Schema({
    title: { type: String, required: true },
    iconRes: { type: String, default: "lamp" }, // <-- Number yerine String yaptık
    isOn: { type: Boolean, default: false },
    level: { type: Number, default: 0 }
    
});

const RoomSchema = new mongoose.Schema({
    name: { type: String, required: true },
    icon: { type: Number, default: 0 },
    devices: [ModuleSchema] // Odanın içindeki cihazlar
});

const Room = mongoose.model('Room', RoomSchema);

// --- SUNUCU AYARLARI ---
const app = express();
const PORT = 3000;

app.use(cors());
app.use(bodyParser.json());

// 🏠 VERİTABANI BAĞLANTISI
// Sonuna '_v2' ekledim, tertemiz veritabanı olacak.
mongoose.connect(process.env.MONGODB_URI || "mongodb://127.0.0.1:27017/netelsan")
    .then(() => console.log("✅ Veritabanına Bağlandı!"))
    .catch((err) => console.log("❌ Hata:", err));


// ==========================================
// 📡 API UÇLARI (ROUTES)
// ==========================================

// 1. TÜM ODALARI VE CİHAZLARI GETİR
app.get('/api/rooms', async (req, res) => {
    try {
        const rooms = await Room.find();
        res.json(rooms);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 2. YENİ ODA EKLE (AKILLI ŞABLON SİSTEMİ)
// ==========================================
app.post('/api/rooms', async (req, res) => {
    try {
        const roomName = req.body.name.trim();

        // 1. Önce bu isimde oda var mı kontrol et
        const existingRoom = await Room.findOne({ 
            name: { $regex: new RegExp(`^${roomName}$`, 'i') } 
        });

        if (existingRoom) {
            return res.status(400).json({ error: "Bu isimde bir oda zaten var!" });
        }

        // 2. ÖZEL ODA ŞABLONLARI (Senin istediğin liste burada)
        // İsimleri küçük harfle kontrol edeceğiz ki "salon" yazsa da tanısın.
        const roomTemplates = {
            "salon": [
                { title: "Avize", iconRes: "lamp", isOn: true, level: 100 },
                { title: "Spot Lambalar", iconRes: "lamp", isOn: false, level: 0 },
                { title: "Salon Kliması", iconRes: "ac", isOn: true, level: 24 },
                { title: "Güneşlik Perde", iconRes: "curtain", isOn: false, level: 0 },
                { title: "Tül Perde", iconRes: "curtain", isOn: true, level: 100 },
                { title: "Salon Kamera", iconRes: "security", isOn: true, level: 0 }
            ],
            "mutfak": [
                { title: "Mutfak Kliması", iconRes: "ac", isOn: false, level: 22 },
                { title: "Mutfak Avize", iconRes: "lamp", isOn: true, level: 100 },
                { title: "Mutfak Kamerası", iconRes: "security", isOn: true, level: 0 },
                { title: "Yangın Sensörü", iconRes: "sensor", isOn: true, level: 0 }
            ],
            "yatak odası": [
                { title: "Ana Işık", iconRes: "lamp", isOn: false, level: 0 },
                { title: "Abajur", iconRes: "lamp", isOn: false, level: 0 },
                { title: "Klima", iconRes: "ac", isOn: false, level: 24 },
                { title: "Yatak Kamerası", iconRes: "security", isOn: true, level: 0 }
            ],
            "çocuk odası": [
                { title: "Tavan Işığı", iconRes: "lamp", isOn: false, level: 0 },
                { title: "Gece Lambası", iconRes: "lamp", isOn: true, level: 50 },
                { title: "Çocuk Kamerası", iconRes: "security", isOn: true, level: 0 }
            ],
            "banyo": [
                { title: "Banyo Işığı", iconRes: "lamp", isOn: false, level: 0 },
                { title: "Su Baskın Sensörü", iconRes: "sensor", isOn: true, level: 0 },
                { title: "Çamaşır Makinesi", iconRes: "lamp", isOn: false, level: 0 }
            ],
             "balkon": [
                 { title: "Balkon Lambası", iconRes: "lamp", isOn: true, level: 100 },
                 { title: "Balkon ledleri ", iconRes: "lamp", isOn: true, level: 50 },
                 { title: "Hareket Sensörü", iconRes: "sensor", isOn: true, level: 0 }
            ]
        };

        // 3. Hangi cihazları ekleyeceğiz?
        // Gelen ismin küçük harfli haline bak (örn: "Salon" -> "salon")
        let devicesToAdd = roomTemplates[roomName.toLowerCase()];

        // Eğer listede yoksa (örn: "Oyun Odası"), standart paketi kullan
        if (!devicesToAdd) {
            devicesToAdd = [
                { title: "Tavan Lambası", iconRes: "lamp", isOn: false, level: 0 },
                { title: "Klima", iconRes: "ac", isOn: false, level: 24 },
                { title: "Perde", iconRes: "curtain", isOn: false, level: 0 }
            ];
        }

        // 4. Odayı Kaydet
        const newRoom = new Room({
            name: roomName,
            icon: req.body.icon,
            iconStr: req.body.iconStr || "room",
            devices: devicesToAdd // 🔥 AKILLI LİSTE BURAYA GİDİYOR
        });

        const savedRoom = await newRoom.save();
        res.json(savedRoom);

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 3. ODAYA CİHAZ EKLE (YENİ! ⭐)
// Kullanımı: /api/rooms/ODA_ID_BURAYA/modules
app.post('/api/rooms/:id/modules', async (req, res) => {
    try {
        const room = await Room.findById(req.params.id);
        if (!room) return res.status(404).send("Oda bulunamadı");

        // Yeni cihazı oluştur
        const newDevice = {
            title: req.body.title,   // "Klima"
            iconRes: req.body.iconRes,
            isOn: false,
            level: 0
        };

        room.devices.push(newDevice); // Listeye ekle
        await room.save(); // Kaydet
        
        res.json(room); // Güncel odayı geri dön
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 4. CİHAZ DURUMUNU GÜNCELLE (AÇ/KAPA) (YENİ! ⭐)
// Kullanımı: /api/rooms/ODA_ID/modules/CIHAZ_ID
app.put('/api/rooms/:roomId/modules/:moduleId', async (req, res) => {
    try {
        const room = await Room.findById(req.params.roomId);
        if (!room) return res.status(404).send("Oda yok");

        // Cihazı bul
        const device = room.devices.id(req.params.moduleId);
        if (!device) return res.status(404).send("Cihaz yok");

        // Değerleri güncelle (Telefondan ne gelirse)
        if (req.body.isOn !== undefined) device.isOn = req.body.isOn;
        if (req.body.level !== undefined) device.level = req.body.level;

        await room.save();
        res.json(device); // Güncel cihazı dön
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ==========================================
// 🚀 TEK TIKLA KURULUM (SEEDING)
// ==========================================
// Tarayıcıdan http://localhost:3000/api/setup adresine git, her şeyi kursun.
app.get('/api/setup', async (req, res) => {
    try {
        // 1. Önce temizlik yap (Eski verileri sil)
        await Room.deleteMany({});

        // 2. Senin Java kodundaki verilerin aynısı:
        const initialData = [
            {
                name: "Salon",
                icon: 2131230935, // R.drawable.television (Android'den bakarız veya string yaparız)
                // İkonları string tutmak daha güvenli: "tv", "kitchen" vs.
                iconStr: "tv", 
                devices: [
                    { title: "Avize", iconRes: "lamp", isOn: true, level: 100 },
                    { title: "Spot Lambalar", iconRes: "lamp", isOn: false, level: 0 },
                    { title: "Salon Kliması", iconRes: "ac", isOn: true, level: 24 },
                    { title: "Güneşlik Perde", iconRes: "curtain", isOn: false, level: 0 },
                    { title: "Tül Perde", iconRes: "curtain", isOn: true, level: 100 },
                    { title: "Salon Kamera", iconRes: "security", isOn: true, level: 0 }
                ]
            },
            {
                name: "Mutfak",
                iconStr: "kitchen",
                devices: [
                    { title: "Mutfak Kliması", iconRes: "ac", isOn: false, level: 22 },
                    { title: "Mutfak Avize", iconRes: "lamp", isOn: true, level: 100 },
                    { title: "Mutfak Kamerası", iconRes: "security", isOn: true, level: 0 },
                    { title: "Yangın Sensörü", iconRes: "sensor", isOn: true, level: 0 }
                ]
            },
            {
                name: "Yatak Odası",
                iconStr: "bedroom",
                devices: [
                    { title: "Ana Işık", iconRes: "lamp", isOn: false, level: 0 },
                    { title: "Abajur", iconRes: "lamp", isOn: false, level: 0 },
                    { title: "Klima", iconRes: "ac", isOn: false, level: 24 },
                    { title: "Yatak Kamerası", iconRes: "security", isOn: true, level: 0 }
                ]
            },
            {
                name: "Çocuk Odası",
                iconStr: "child",
                devices: [
                    { title: "Tavan Işığı", iconRes: "lamp", isOn: false, level: 0 },
                    { title: "Gece Lambası", iconRes: "lamp", isOn: true, level: 50 },
                    { title: "Çocuk Kamerası", iconRes: "security", isOn: true, level: 0 }
                ]
            },
            {
                name: "Banyo",
                iconStr: "bath",
                devices: [
                    { title: "Banyo Işığı", iconRes: "lamp", isOn: false, level: 0 },
                    { title: "Su Baskın Sensörü", iconRes: "sensor", isOn: true, level: 0 },
                    { title: "Çamaşır Makinesi", iconRes: "lamp", isOn: false, level: 0 }
                ]
            },
            {
                name: "Balkon",
                iconStr: "entry",
                devices: [
                    { title: "Balkon Lambası", iconRes: "lamp", isOn: true, level: 100 },
                    { title: "Balkon ledleri ", iconRes: "lamp", isOn: true, level: 50 },
                    { title: "Hareket Sensörü", iconRes: "sensor", isOn: true, level: 0 }
                ] 
            }
        ];

        // 3. Hepsini veritabanına kaydet
        // (Not: iconStr alanını şemana eklemen lazım, yoksa sadece cihazlar kaydolur)
        // Eğer Room şemanda sadece 'icon' (number) varsa, geçici olarak 'icon' alanına rastgele sayı atarız.
        // Ama en temizi Android tarafında string yönetmektir.
        
        // Veriyi MongoDB formatına uyduruyoruz:
        for (const roomData of initialData) {
            const newRoom = new Room({
                name: roomData.name,
                // Şimdilik rastgele bir ID veriyorum, Android bunu isme göre düzeltecek
                icon: 0, 
                // Cihazları ekle
                devices: roomData.devices
            });
            // Odanın ikon ismini (tv, kitchen vs.) cihaz listesine gizli bir "parametre" gibi ekleyelim mi?
            // Veya en kolayı: RoomSchema'ya "iconStr" eklemiştik hatırlarsan?
            // Eklememişsek bile cihazlar eklenecek.
            await newRoom.save();
        }

        res.send("✅ KURULUM TAMAMLANDI! Tüm odalar ve cihazlar yüklendi.");

    } catch (err) {
        res.status(500).send("Hata: " + err.message);
    }
});

// 🗑️ ODA SİLME (DELETE)
app.delete('/api/rooms/:id', async (req, res) => {
    try {
        const deletedRoom = await Room.findByIdAndDelete(req.params.id);
        if (!deletedRoom) return res.status(404).send("Oda bulunamadı");
        res.json({ message: "Oda başarıyla silindi", id: req.params.id });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


// --- BAŞLAT ---
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Sunucu Hazır: http://localhost:${PORT}`);
});