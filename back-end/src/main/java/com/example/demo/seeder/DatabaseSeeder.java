package com.example.demo.seeder;

import com.example.demo.Feedback.entity.Feedback;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Feedback.entity.FeedbackStatus;
import com.example.demo.Feedback.entity.FeedbackTypes;
import com.example.demo.Feedback.repository.FeedbackRepository;
import com.example.demo.Feedback.repository.FeedbackStatusRepository;
import com.example.demo.Feedback.repository.FeedbackTypesRepository;
import com.example.demo.MapSearch.service.GeocodingService;
import com.example.demo.admin.Admin;
import com.example.demo.admin.AdminPosition;
import com.example.demo.admin.AdminRepository;
import com.example.demo.shop.entity.OrderDetails;
import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.entity.Products;
import com.example.demo.shop.entity.Stock;
import com.example.demo.shop.repository.ProductsRepository;
import com.example.demo.store.entity.Category;
import com.example.demo.store.entity.StoresInfo;
import com.example.demo.store.repository.CategoryRepository;
import com.example.demo.store.repository.StoreInfoRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import net.datafaker.Faker;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StoreInfoRepository storeInfoRepository;
    private final AdminRepository adminRepository;
    private final FeedbackStatusRepository feedbackStatusRepository;
    private final FeedbackTypesRepository feedbackTypesRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeocodingService geocodingService;
    private final ProductsRepository productsRepository;
    private final FeedbackRepository feedbackRepository;
    private final com.example.demo.shop.repository.OrdersRepository ordersRepository;
    private final com.example.demo.shop.repository.OrderDetailsRepository orderDetailsRepository;

    @Value("${app.seeder.enabled:false}")
    private boolean seederEnabled;

    public DatabaseSeeder(
            UserRepository userRepository,
            StoreInfoRepository storeInfoRepository,
            AdminRepository adminRepository,
            FeedbackStatusRepository feedbackStatusRepository,
            FeedbackTypesRepository feedbackTypesRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder,
            GeocodingService geocodingService,
            ProductsRepository productsRepository,
            FeedbackRepository feedbackRepository,
            com.example.demo.shop.repository.OrdersRepository ordersRepository,
            com.example.demo.shop.repository.OrderDetailsRepository orderDetailsRepository) {
        this.userRepository = userRepository;
        this.storeInfoRepository = storeInfoRepository;
        this.adminRepository = adminRepository;
        this.feedbackStatusRepository = feedbackStatusRepository;
        this.feedbackTypesRepository = feedbackTypesRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.geocodingService = geocodingService;
        this.productsRepository = productsRepository;
        this.feedbackRepository = feedbackRepository;
        this.ordersRepository = ordersRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!seederEnabled) {
            System.out.println("Data Seeder is disabled.");
            return;
        }

        System.out.println("Starting to seed database...");

        seedFeedbackStatus();
        seedFeedbackTypes();
        seedCategories();
        seedAdmins();
        seedUsersAndStores(); // 產生User與Stores
        seedProducts(); // 來自原本的 DataInitializer
        seedFeedbacks(); // 產生Feedback資料
        seedOrders(); //產生orders

        System.out.println("Database seeding completed.");
    }

    private void seedFeedbackStatus() {
        if (feedbackStatusRepository.count() == 0) {
            List<String> statuses = Arrays.asList("待處理", "已處理", "待致電", "追蹤中");
            for (String statusName : statuses) {
                FeedbackStatus status = new FeedbackStatus();
                status.setStatusName(statusName);
                feedbackStatusRepository.save(status);
            }
            System.out.println("Seeded 4 FeedbackStatuses.");
        }
    }

    private void seedFeedbackTypes() {
        if (feedbackTypesRepository.count() == 0) {
            List<String> types = Arrays.asList("意見回饋", "商品上架", "餐廳訂位", "商品訂單", "問題通報", "表揚讚美", "其他");
            for (String typeName : types) {
                FeedbackTypes type = new FeedbackTypes();
                type.setTypeName(typeName);
                feedbackTypesRepository.save(type);
            }
            System.out.println("Seeded 5 FeedbackTypes.");
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<String> categories = Arrays.asList("親子友善", "寵物友善", "綠色宣言餐廳", "公平貿易咖啡豆", "蔬食", "無障礙設施", "有停車場", "提供插座");
            for (String catName : categories) {
                Category category = new Category();
                category.setCategoryName(catName);
                categoryRepository.save(category);
            }
            System.out.println("Seeded 8 Categories.");
        }
    }

    private void seedAdmins() {
        if (adminRepository.count() == 0) {
            String defaultPassword = passwordEncoder.encode("1qaz@WSX");

            // {account, name, email, positionName}
            String[][] adminData = {
                    { "sa", "測試總管理員", "sa@admin.com", "SUPER_ADMIN" },
                    { "hr", "測試人事", "hr@admin.com", "HUMAN_RESOURCE" },
                    { "cs", "測試客服", "cs@admin.com", "CUSTOMER_SERVICE" },
                    { "sh", "測試電商", "sh@admin.com", "SHOP_MANAGER" }
            };

            for (String[] data : adminData) {
                AdminPosition position = AdminPosition.valueOf(data[3]);
                Admin admin = Admin.builder()
                        .account(data[0])
                        .password(defaultPassword)
                        .name(data[1])
                        .position(position)
                        .email(data[2])
                        .enabled(true)
                        .build();
                adminRepository.save(admin);
            }
            System.out.println("Seeded 4 Admins.");
        }
    }

    private void seedUsersAndStores() {
        if (userRepository.count() == 0) {
            Faker faker = new Faker(Locale.TAIWAN);
            String defaultPassword = passwordEncoder.encode("1qaz@WSX");

            // 預設的使用者名稱與信箱列表
            String[][] userData = {
                    { "Liam", "liam@test.com" },
                    { "Noah", "noah@test.com" },
                    { "Oliver", "oliver@test.com" },
                    { "Elijah", "elijah@test.com" },
                    { "James", "james@test.com" },
                    { "William", "william@test.com" },
                    { "Benjamin", "benjamin@test.com" },
                    { "Lucas", "lucas@test.com" },
                    { "Henry", "henry@test.com" },
                    { "Alexander", "alexander@test.com" },
                    { "Mason", "mason@test.com" },
                    { "Michael", "michael@test.com" },
                    { "Ethan", "ethan@test.com" },
                    { "Daniel", "daniel@test.com" },
                    { "Jacob", "jacob@test.com" },
                    { "Logan", "logan@test.com" },
                    { "Jackson", "jackson@test.com" },
                    { "Levi", "levi@test.com" },
                    { "Sebastian", "sebastian@test.com" },
                    { "Mateo", "mateo@test.com" }, // 第20筆 (含) 之前是 Store User
                    { "Jack", "jack@test.com" }, // 第21筆起是 Normal User
                    { "Owen", "owen@test.com" },
                    { "Theodore", "theodore@test.com" },
                    { "Aiden", "aiden@test.com" },
                    { "Samuel", "samuel@test.com" },
                    { "Joseph", "joseph@test.com" },
                    { "John", "john@test.com" },
                    { "David", "david@test.com" },
                    { "Wyatt", "wyatt@test.com" },
                    { "Matthew", "matthew@test.com" }
            };

            List<User> storeUsers = new ArrayList<>();
            List<User> normalUsers = new ArrayList<>();

            // 1. 產生 30 筆 User，前 20 筆 isStore=true，後 10 筆 isStore=false
            for (int i = 0; i < userData.length; i++) {
                boolean isStore = i < 20;
                User user = User.builder()
                        .name(userData[i][0])
                        .email(userData[i][1])
                        .password(defaultPassword)
                        .isStore(isStore)
                        .enabled(true)
                        .twoFactorEnabled(false)
                        .build();

                if (isStore) {
                    storeUsers.add(user);
                } else {
                    normalUsers.add(user);
                }
            }

            // 儲存所有 Users
            storeUsers = userRepository.saveAll(storeUsers);
            userRepository.saveAll(normalUsers);
            System.out.println("Seeded 30 Users (20 Store Users, 10 Normal Users).");

            // 2. 針對 20 名 Store User 產生 StoresInfo
            String[][] storeData = {
                    { "日初壽司屋", "新北市中和區中山路三段122號", "02-2721-5508" },
                    { "山海樓私房料理", "新北市三重區重新路二段21號", "04-2326-8801" },
                    { "老王滷肉飯", "桃園市桃園區中正路47號", "02-2556-0931" },
                    { "森林早午餐", "新北市淡水區中山路8號", "0978-351-206" },
                    { "古早味排骨飯", "高雄市前金區中華三路21號", "04-2231-5094" },
                    { "川味麻婆食堂", "南投縣草屯鎮太平路二段294號", "04-2228-3157" },
                    { "港都海產店", "桃園市龜山區復興一路8號", "07-551-3372" },
                    { "巷弄義麵屋", "台南市永康區中正南路358號", "0972-604-773" },
                    { "藍海景觀餐廳", "屏東縣屏東市民生路295號", "03-978-6612" },
                    { "金色三麥餐酒館", "南投縣埔里鎮中正路399號", "0937-115-624" },
                    { "米香台菜餐廳", "台中市西屯區台灣大道四段1038號", "02-2395-8210" },
                    { "香辣川味館", "台北市士林區中正路115號", "03-422-5631" },
                    { "海港鮮味餐廳", "台北市信義區松壽路12號", "07-332-9184" },
                    { "阿婆鹹酥雞", "雲林縣虎尾鎮林森路一段491號", "0916-875-320" },
                    { "暖心鍋物", "新北市新莊區中正路425號", "0933-642-851" },
                    { "富士山丼飯", "高雄市鳳山區中山路149號", "0914-992-385" },
                    { "小時光甜點店", "金門縣金城鎮民生路45號", "0975-210-943" },
                    { "滿福餃子館", "台中市北區三民路三段161號", "0955-481-903" },
                    { "龍門客棧川菜", "花蓮縣花蓮市中山路295號", "02-2388-1723" },
                    { "金月韓式料理", "台南市安平區華平路711號", "02-2735-6219" }
            };

            List<Category> allCategories = categoryRepository.findAll();
            for (int i = 0; i < 20; i++) {
                User storeUser = storeUsers.get(i);
                StoresInfo store = new StoresInfo();
                store.setUser(storeUser);
                store.setStoreName(storeData[i][0]);
                store.setAddress(storeData[i][1]);
                store.setStorePhone(storeData[i][2]);
                store.setDescription(faker.lorem().paragraph());

                // Image Logic: store0.jpg ~ store5.jpg
                store.setCoverImage("store" + (i % 6) + ".jpg");

                // 使用 GeocodingService 將地址轉換成經緯度
                BigDecimal[] coords = geocodingService.geocode(storeData[i][1]);
                if (coords != null && coords.length == 2) {
                    store.setLatitude(coords[0]);
                    store.setLongitude(coords[1]);
                } else {
                    // 若獲取失敗則預設給一個經緯度
                    store.setLatitude(BigDecimal.valueOf(22.0 + Math.random() * 3.0));
                    store.setLongitude(BigDecimal.valueOf(120.0 + Math.random() * 2.0));
                }

                // 隨機關聯 1~3 個 Categories
                int numCategories = faker.number().numberBetween(1, 4);
                List<Category> randomCategories = new ArrayList<>();
                for (int c = 0; c < numCategories; c++) {
                    Category randomCat = allCategories.get(faker.number().numberBetween(0, allCategories.size()));
                    if (!randomCategories.contains(randomCat)) {
                        randomCategories.add(randomCat);
                    }
                }
                store.setCategories(randomCategories);

                storeInfoRepository.save(store);
            }
            System.out.println("Seeded 20 Stores for the 20 Store Users.");
        }
    }

    private void seedProducts() {
        if (productsRepository.count() > 0) {
            return;
        }

        System.out.println("Starting to seed products...");

        saveProduct("【小時光甜點店】盛宴．森林系莓果塔", "食品", new BigDecimal("1080"),
            "如同漫步於雨後的森林小徑，這是一款視覺與味覺雙重享受的甜點。\r\n" + //
            "\r\n" + //
            "我們選用優質天然發酵奶油製作塔皮，搭配香氣濃郁的本土萊姆，打造出絲滑細緻的內餡。塔面裝飾如同一座繽紛的小花園，莓果的微酸與檸檬的清新完美融合，每一口都能感受到大自然的原始鮮甜。適合與一杯熱紅花茶共同享用，讓下午茶時光變得優雅且放鬆。\r\n" + //
            "", 10, "/productPictures/cake.jpg");

        saveProduct("限量小農鮮蔬盒", "生鮮", new BigDecimal("499"),
            "嚴選在地小農友善耕作蔬菜，新鮮現採直送。無毒栽培降低農藥殘留風險，吃得更安心。支持在地農業同時減少長途運輸碳排放。", 2, "/productPictures/VeggieBox.jpg");

        saveProduct("【季節限定】柿柿如意鮮果禮盒", "生鮮", new BigDecimal("799"),
            "精選當季新鮮柿子，由在地農民用心栽種。自然熟成風味香甜，無過度人工催熟處理。產地直送縮短運輸流程，保留最佳鮮度。", 30, "/productPictures/persimmon.jpg");
        
        saveProduct("【金月韓式料理】匠心手作雙拼泡菜", "食品", new BigDecimal("180"),
            "傳承韓式正統發酵工法，嚴選鮮甜大白菜與清脆白蘿蔔，每日由主廚手工揉製。\r\n" + //
            "韓式大白菜泡菜： 葉片完整吸收韓國進口辣椒粉的辛香，入喉帶有淡淡的果香甘甜，酸爽開胃。\r\n" + //
            "韓式脆口辣蘿蔔： 切成易入口的正方形丁塊，保留了蘿蔔原始的清甜與爽脆咬勁，是餐桌上最解膩的配角。\r\n" + //
            "堅持無添加防腐劑，富含益生菌。無論是單吃當小菜、拌飯、或是加入湯底做成泡菜鍋，都能品嚐到最道地的韓式風味。\r\n" + //
            "。", 55, "/productPictures/Pickle.jpg");

        saveProduct("【海港鮮味餐廳】產地直送冷凍海鮮", "生鮮", new BigDecimal("2680"),
            "將大海的恩惠完美封存，【海港鮮味餐廳】嚴選當季最肥美的漁獲，捕撈後立即進行低溫極速冷凍，鎖住每一分原始鮮甜，讓您在家也能享有如漁港現場般的極致美味。\r\n" + //
            "\r\n" + //
            "頂級刺身級鮪魚： 色澤鮮紅、油脂分布均勻，肉質細嫩，解凍後即可品嚐如同現捕的鮮美。\r\n" + //
            "\r\n" + //
            "極鮮野生海蝦與透抽： 蝦肉飽滿 Q 彈，透抽肉質紮實，簡單清蒸或汆燙即可品嚐其清脆甘甜。\r\n" + //
            "\r\n" + //
            "當季旬魚系列： 包含現撈鮮魚及精選細鱗漁獲，魚眼清亮，鱗片完整，適合烤、炸或煮湯，展現多變料理風味。\r\n" + //
            "\r\n" + //
            "豪華甲殼類與貝類： 嚴選肥美毛蟹與厚實貝類，肉質細緻且富含海味精華，是升級餐桌饗宴的靈魂食材。\r\n" + //
            "\r\n" + //
            "品質保證： 全程低溫物流配送，堅持無添加、無化學保鮮，提供您最安心、最純淨的海洋鮮味。", 35, "/productPictures/seafood.jpg");

        saveProduct("田野直送鮮蔬箱", "生鮮", new BigDecimal("529"),
            "嚴選在地小農每日現採蔬菜，新鮮直送到家，保留最自然的風味與營養採用友善耕作方式種植，減少農藥與化學肥料使用。", 40, "/productPictures/Veggie.jpg");

        saveProduct("【山海樓私房料理】傳承．中式饗宴套餐", "食品", new BigDecimal("2880"),
            "匯集多款主廚拿手好菜，將傳統台式與港式精髓完美揉合，讓您一次品嚐多層次的珍饈美味。\r\n" + //
            "紅燒排翅(盅)： 選用頂級食材，經長時火候慢燉，湯頭濃郁滑順，每一口都能感受到膠質的細膩與鮮甜。\r\n" + //
            "港式點心拼盤： 包含皮薄餡亮的晶瑩蝦餃與飽滿紮實的招牌燒賣，口口彈牙，盡顯真功夫。\r\n" + //
            "川味口水雞： 嚴選鮮嫩雞肉，搭配特製香辣醬汁與清爽黃瓜片，麻、辣、鮮、香四味一體，極其開胃。\r\n" + //
            "糖醋鮮蝦球： 外皮酥脆、蝦肉Q彈，裹上特調酸甜芡汁，是大人小孩都喜愛的經典滋味。\r\n" + //
            "私房時令熱炒： 搭配當季鮮蔬與主廚推薦小品，為整場饗宴畫下清爽圓滿的句點。\r\n" + //
            "", 30, "/productPictures/chineseFood.jpg");

        saveProduct("旬採鮮果禮盒", "生鮮", new BigDecimal("649"),
                "精選當季成熟水果，由小農自然栽培，無過度催熟與人工加工。果實在最佳熟度採收，保留最純粹甜味與香氣。", 35, "/productPictures/fruit.jpg");

        saveProduct("永恆工藝．循環餐具組【流光金】", "日常用品", new BigDecimal("680"),
                "霧面金質感設計，兼具時尚與耐用性，外食或露營都適用。採用可重複使用材質製作，大幅減少一次性餐具浪費。", 60, "/productPictures/gold.jpg");

        saveProduct("永恆工藝．循環餐具組【月光銀】", "日常用品", new BigDecimal("580"),
                "俐落極簡外型搭配高耐用金屬材質，長期使用不易損耗。適合上班族、學生與外食族隨身攜帶。", 60, "/productPictures/silver.jpg");

        saveProduct("植萃癒合．三效美體護理組(三合一)", "日常用品", new BigDecimal("1880"),
            "結合乳液、精華油與面霜三步驟完整保養。植物來源成分溫和親膚，同時降低對環境負擔。深層滋養與鎖水保濕一次完成。", 25, "/productPictures/cream.jpg");
        
        saveProduct("【米香台菜餐廳】秘製傳承．無錫排骨", "食品", new BigDecimal("420"),
            "這道「無錫排骨」是米香台菜的經典功夫菜，展現了燉煮工藝的極致實力。\r\n" + //
            "嚴選食材： 挑選肥瘦比例完美的溫體豬腩排，保留帶骨的香氣與軟嫩的肉質。\r\n" + //
            "工序繁複： 經過先炸後滷的多道手續，鎖住肉汁並去除多餘油脂。加入陳年紹興與獨門辛香料慢火細燉數小時，直到骨肉輕易分離。\r\n" + //
            "層次風味： 醬汁濃稠透亮，入口鹹甜適中、醇厚不膩，搭配清鮮翠綠的青江菜，不僅平衡了口感，更襯托出醬香的深度。\r\n" + //
            "主廚推薦： 這道菜濃郁的醬汁非常適合搭配白飯，是老饕們心中不可替代的「白飯殺手」。", 20, "/productPictures/meat.jpg");

            saveProduct("【米香台菜餐廳】橙香御品獅子頭", "食品", new BigDecimal("380"),
            "打破傳統紅燒的厚重感，主廚巧妙將新鮮果香融入經典肉丸，打造視覺與味覺的雙重饗宴。\r\n" + //
            "職人手打肉丸： 嚴選黃金比例的豬後腿肉，經由數百次甩打確保肉質Ｑ彈紮實，入口即化的同時保有彈牙嚼勁。\r\n" + //
            "橙汁秘製芡汁： 捨棄傳統重油重鹹的調味，改以新鮮柳橙汁為基底慢火熬製琥珀色醬汁，酸甜甘美，完美平衡肉質的醇厚。\r\n" + //
            "清新風味組合： 盤底襯以鮮甜橙片，讓每一口果香隨著熱氣滲入肉丸中心，呈現層次分明的清新台式風味，是宴席上老少咸宜的最佳選擇。\r\n" + //
            "", 20, "/productPictures/meatBall.jpg");


        saveProduct("【天然原萃】手作無添加果糖(減糖)", "食品", new BigDecimal("250"),
                "僅使用純天然水果與零卡替代糖，不含人工色素與防腐劑。口感自然清甜，適合大人小孩安心享用。採用可回收包裝。", 80, "/productPictures/candy.jpg");

        saveProduct("【大地秘境】產地直送純淨辛香料", "食品", new BigDecimal("480"),
                "嚴選自永續農法認證在地小農，不噴灑化學農藥。100%純粹，絕不添加防腐劑、人工色素、味精。採用自然風乾與低溫研磨技術。", 70, "/productPictures/spices.jpg");

        saveProduct("純淨補水．Tritan 永續運動瓶", "日常用品", new BigDecimal("450"),
                "輕量防漏運動水杯(800ml)，醫療級 Tritan™ 材質，不含雙酚 A (BPA Free)。減少瓶裝水依賴，即便損壞也可回收處理。", 45,
                "/productPictures/bottle.jpg");

        saveProduct("植感定型．天然蠟豆造型髮蠟", "日常用品", new BigDecimal("420"),
                "採用植物蠟與天然油脂配方，好清洗、不殘留，不造成頭皮負擔。不排放難分解化學物質，讓造型也很有環保意識。", 55, "/productPictures/hairWax.jpg");

        saveProduct("愛地球環保摺疊紙袋", "日常用品", new BigDecimal("80"),
                "採用環保再生紙材製成，可自然分解，減少塑膠袋對環境造成的負擔。袋身厚實耐用，承重力佳，購物、外出、送禮皆適用。", 200, "/productPictures/bag.jpg");

        System.out.println("Seeded 12 Products.");
    }

    private void seedFeedbacks() {
        if (feedbackRepository.count() > 0) {
            return;
        }

        System.out.println("Starting to seed feedbacks...");

        FeedbackStatus status = feedbackStatusRepository.findAll().stream()
                .filter(s -> s.getStatusName().equals("待處理"))
                .findFirst().orElse(null);

        List<FeedbackTypes> allTypes = feedbackTypesRepository.findAll();

        // 1. User A~E (userId 1~5)
        String[][] userFeedbacks = {
                { "介面設計很清爽，特別喜歡地圖篩選功能，能快速找到附近隱藏的排隊名店，希望未來能增加收藏夾分類。", "意見回饋" },
                { "建議在餐廳頁面增加「素食友善」的標籤，這樣帶長輩聚餐時篩選會更方便，整體使用體驗非常流暢。", "意見回饋" },
                { "饗島的懶人包推薦非常有深度，不是那種常見的業配文，希望能持續更新更多關於台灣在地小吃的專題。", "意見回饋" },
                { "訂位系統反應很快，送出申請後馬上就收到簡訊確認，當天到店出示畫面就能入座，完全不用排隊。", "餐廳訂位" },
                { "幫全家人預約了週末的熱炒店，系統自動提醒功能很貼心，對於我們這種常忘記時間的人來說非常實用。", "餐廳訂位" }
        };

        int count = 1;
        for (int i = 0; i < 5; i++) {
            User user = userRepository.findById((long) (i + 1)).orElse(null);
            if (user != null) {
                Feedback fb = new Feedback();
                fb.setCaseNumber(String.format("FB202611%07d", count++));
                fb.setName(user.getName());
                fb.setEmail(user.getEmail());
                fb.setPhone("0900-111-22" + i);
                fb.setContents(userFeedbacks[i][0]);
                final int index = i;
                fb.setFeedbackTypes(allTypes.stream().filter(t -> t.getTypeName().equals(userFeedbacks[index][1]))
                        .findFirst().orElse(null));
                fb.setFeedbackStatus(status);
                fb.setUser(user);
                feedbackRepository.save(fb);
            }
        }

        // 2. Anonymous feedback
        // 姓名, Email, Phone, Contents, Type
        String[][] anonymousData = {
                { "陳志明", "cm.chen88@gmail.com", "0912-345-678", "介面操作直覺，連家裡的長輩都能自己操作訂位，這點對推廣在地美食真的很有幫助，非常推薦。", "餐廳訂位" },
                { "Lin, Sophia", "sophia.lin_tw@outlook.com", "0921-987-654",
                        "在商城買的在地小農伴手禮包裝得很精美，配送速度也比預期快，能在平台上一站式購足美食真的很方便。", "商品訂單" },
                { "王大同", "tatung.wang@islandfood.com.tw", "0933-111-222",
                        "買了饗島推薦的麻辣火鍋湯底，味道跟在店裡吃的一模一樣，物流過程商品保護得很好，沒有滲漏情形。", "商品訂單" },
                { "Chang, Kevin", "kevin_chang99@me.com", "0955-888-777",
                        "這次訂購的冷凍宅配組出貨通知很即時，讓我可以準確預約收貨時間，是值得信賴的電商購物體驗。", "商品訂單" },
                { "李美玲", "meiling_li_1985@yahoo.com.tw", "0910-555-444",
                        "剛才嘗試在上傳評論照片時，App 出現閃退狀況，我的手機型號是 iPhone 15，希望開發團隊能協助排查。", "問題通報" },
                { "Wu, Jessica", "jessy.wu@protonmail.com", "0972-333-666",
                        "發現地圖上某家牛肉麵店的營業時間與實際不符，店面似乎已經搬遷，請確認資訊並更新，以免其他用戶撲空。", "問題通報" },
                { "黃小龍", "dragon.huang_official@gmail.com", "0988-168-168",
                        "在進行訂單付款時，頁面跳轉至第三方支付偶爾會卡住，建議優化金流串接的穩定度，避免重複扣款疑慮。", "問題通報" },
                { "Hsu, Peter", "peter_hsu_tech@fastmail.com", "0928-456-123",
                        "饗島真的是吃貨救星！客服回覆問題專業且充滿溫度，能感覺到團隊是真的熱愛台灣美食，加油！", "表揚讚美" },
                { "蔡雅婷", "yating.tsai_foodie@gmail.com", "0963-777-999",
                        "感謝饗島幫我找到家鄉失傳已久的味道，那個地圖定位精準度真的沒話說，是我手機裡最重要的 App。", "表揚讚美" },
                { "Yang, David", "d.yang_taiwan@icloud.com", "0919-222-333",
                        "這次透過平台安排的環島美食行程大受好評，整合地圖與訂位的設計太聰明了，謝謝你們開發這麼棒的工具。", "表揚讚美" }
        };

        for (String[] data : anonymousData) {
            Feedback fb = new Feedback();
            fb.setCaseNumber(String.format("FB202611%07d", count++));
            fb.setName(data[0]);
            fb.setEmail(data[1]);
            fb.setPhone(data[2]);
            fb.setContents(data[3]);
            fb.setFeedbackTypes(
                    allTypes.stream().filter(t -> t.getTypeName().equals(data[4])).findFirst().orElse(null));
            fb.setFeedbackStatus(status);
            feedbackRepository.save(fb);
        }

        System.out.println("Seeded 15 Feedbacks.");
    }

    private void saveProduct(String name, String category, BigDecimal price, String description, int stockQty,
            String image) {
        Products p = new Products();
        p.setProductName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setProductDescription(description);
        p.setImage(image);

        Stock stock = new Stock();
        stock.setAvailableQuantity(stockQty);
        stock.setProduct(p);
        p.setStock(stock);

        Products saved = productsRepository.save(p);
        saved.setProductCode(String.format("PRD-%04d", saved.getProductId()));
        
        productsRepository.save(saved);
    }

    private void seedOrders() {
    if (ordersRepository.count() > 0) {
        return;
    }

    System.out.println("Starting to seed orders...");
    Faker faker = new Faker(Locale.TAIWAN);
    
    
    List<User> normalUsers = userRepository.findAll().stream()
        .filter(u -> u.getIsStore() != null && !u.getIsStore()) 
        .toList();
    List<Products> allProducts = productsRepository.findAll();
    
    if (normalUsers.isEmpty() || allProducts.isEmpty()) {
        System.out.println("Skip seeding orders: No normal users or products found.");
        return;
    }

    List<String> statuses = Arrays.asList("待付款", "待出貨", "處理中", "已完成");
    List<String> payMethods = Arrays.asList("ECpay", "cod");

    for (int i = 0; i < 15; i++) {
        User buyer = normalUsers.get(faker.number().numberBetween(0, normalUsers.size()));
        String currentStatus = statuses.get(i % statuses.size());

        // A. 建立訂單主檔
        Orders order = new Orders();
        order.setUser(buyer);
        order.setReceiverName(buyer.getName());
        order.setReceiverPhone("09" + faker.number().digits(8));
        order.setReceiverAddress(faker.address().fullAddress());
        order.setNote(faker.lorem().sentence());
        order.setStatus(currentStatus);
        order.setPayMethod(payMethods.get(faker.number().numberBetween(0, payMethods.size())));
        order.setDeliveryMethod("宅配");
        String tradeNo = "ORD" + System.currentTimeMillis() + i;
        order.setMerchantTradeNo(tradeNo);
        
        
        // 建立時間與付款時間 (已出貨才給付款時間)
        order.setCreatedAt(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)));
        if ("已出貨".equals(currentStatus)) {
            order.setPaymentDate(Instant.now().minus(java.time.Duration.ofDays(1)));
        }

        
        Orders savedOrder = ordersRepository.save(order);

        int detailCount = faker.number().numberBetween(1, 4);
        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        List<OrderDetails> detailsList = new ArrayList<>();

        for (int j = 0; j < detailCount; j++) {
            Products product = allProducts.get(faker.number().numberBetween(0, allProducts.size()));
            int qty = faker.number().numberBetween(1, 3);
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(qty));

            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setOrderQuantity(qty);
            detail.setProductNameSnapshot(product.getProductName());
            detail.setPriceSnapshot(product.getPrice());
            detail.setSubtotal(subtotal);
            detail.setCommentsSection("顧客備註: " + faker.funnyName().name());
            
            orderDetailsRepository.save(detail); 
            totalOrderPrice = totalOrderPrice.add(subtotal);
        }

        savedOrder.setTotalPrice(totalOrderPrice);
        ordersRepository.save(savedOrder);
    }
    System.out.println("Seeded 15 Orders with complete details.");
}

}
