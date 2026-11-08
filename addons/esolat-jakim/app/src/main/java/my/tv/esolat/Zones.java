package my.tv.esolat;

/**
 * JAKIM prayer zones as MPT serves them (https://mpt.i906.my): the zone's JAKIM code, the state it
 * is in, the MPT code the times are fetched with, and the districts it covers, so the settings
 * screen can offer negeri then zon. Every MPT code here was verified to answer with the JAKIM zone
 * in its attributes.
 */
public final class Zones {
    public static final class Zone {
        public final String jakim, state, mpt, desc;
        public final double lat, lon;

        Zone(String jakim, String state, String mpt, String desc) {
            this(jakim, state, mpt, desc, 0, 0);
        }

        Zone(String jakim, String state, String mpt, String desc, double lat, double lon) {
            this.jakim = jakim;
            this.state = state;
            this.mpt = mpt;
            this.desc = desc;
            this.lat = lat;
            this.lon = lon;
        }

        /** "WLY01 — Kuala Lumpur, Putrajaya" */
        public String titled() {
            return jakim + " — " + desc;
        }
    }

    public static final String[] STATES = {
            "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak", "Perlis",
            "Pulau Pinang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Wilayah Persekutuan"
    };

    public static final Zone[] ZONES = {
            // Johor
            new Zone("JHR01", "Johor", "jhr-9", "Pulau Aur & Pemanggil", 2.45, 104.5),
            new Zone("JHR02", "Johor", "jhr-2", "Johor Bahru, Kota Tinggi, Mersing, Kulai, Pasir Gudang", 1.49, 103.74),
            new Zone("JHR03", "Johor", "jhr-3", "Kluang, Pontian, Simpang Renggam, Benut", 2.03, 103.32),
            new Zone("JHR04", "Johor", "jhr-0", "Batu Pahat, Muar, Segamat, Gemas, Yong Peng", 1.85, 102.93),
            // Kedah
            new Zone("KDH01", "Kedah", "kdh-2", "Kota Setar, Kubang Pasu, Pokok Sena (Alor Setar, Jitra)", 6.12, 100.37),
            new Zone("KDH02", "Kedah", "kdh-12", "Yan, Kuala Muda, Pendang (Sungai Petani)", 5.65, 100.49),
            new Zone("KDH03", "Kedah", "kdh-11", "Sik, Padang Terap, Kuala Nerang", 5.88, 100.73),
            new Zone("KDH04", "Kedah", "kdh-0", "Baling, Kuala Ketil", 5.68, 100.92),
            new Zone("KDH05", "Kedah", "kdh-5", "Kulim, Bandar Baharu, Serdang, Lunas, Padang Serai", 5.37, 100.55),
            new Zone("KDH06", "Kedah", "kdh-6", "Langkawi", 6.35, 99.8),
            new Zone("KDH07", "Kedah", "kdh-10", "Puncak Gunung Jerai", 5.79, 100.43),
            // Kelantan (one zone: JAKIM merged the old KTN02/KTN03 into KTN01)
            new Zone("KTN01", "Kelantan", "ktn-0", "Seluruh Kelantan (Kota Bharu, Pasir Mas, Kuala Krai, Machang, Tumpat)", 6.13, 102.24),
            // Melaka
            new Zone("MLK01", "Melaka", "mlk-1", "Bandar Melaka, Alor Gajah, Jasin, Masjid Tanah, Merlimau", 2.19, 102.25),
            // Negeri Sembilan
            new Zone("NGS01", "Negeri Sembilan", "ngs-1", "Jempol, Tampin, Bahau, Gemencheh", 2.73, 102.37),
            new Zone("NGS02", "Negeri Sembilan", "ngs-4", "Seremban, Port Dickson, Nilai, Rembau, Kuala Pilah, Jelebu", 2.73, 101.94),
            // Pahang
            new Zone("PHG01", "Pahang", "phg-12", "Pulau Tioman", 2.79, 104.17),
            new Zone("PHG02", "Pahang", "phg-8", "Kuantan, Pekan, Rompin, Muadzam Shah, Gambang", 3.81, 103.33),
            new Zone("PHG03", "Pahang", "phg-1", "Temerloh, Maran, Jerantut, Bera, Jengka", 3.45, 102.42),
            new Zone("PHG04", "Pahang", "phg-7", "Bentong, Raub, Kuala Lipis, Mentakab, Lanchang", 3.52, 101.91),
            new Zone("PHG05", "Pahang", "phg-17", "Janda Baik, Genting Sempah, Bukit Tinggi", 3.41, 101.85),
            new Zone("PHG06", "Pahang", "phg-3", "Cameron Highlands, Bukit Fraser, Genting Highlands", 4.47, 101.38),
            // Perak
            new Zone("PRK01", "Perak", "prk-24", "Tapah, Slim River, Tanjung Malim, Sungkai, Bidor", 4.2, 101.26),
            new Zone("PRK02", "Perak", "prk-2", "Ipoh, Batu Gajah, Kampar, Kuala Kangsar, Sungai Siput", 4.6, 101.07),
            new Zone("PRK03", "Perak", "prk-15", "Grik, Lenggong, Pengkalan Hulu", 5.3, 101.0),
            new Zone("PRK04", "Perak", "prk-3", "Belum, Temengor", 5.42, 101.34),
            new Zone("PRK05", "Perak", "prk-0", "Lumut, Sitiawan, Teluk Intan, Manjung, Bagan Datoh", 4.23, 100.63),
            new Zone("PRK06", "Perak", "prk-1", "Taiping, Parit Buntar, Bagan Serai, Selama, Kamunting", 4.85, 100.73),
            new Zone("PRK07", "Perak", "prk-5", "Bukit Larut (Maxwell Hill)", 4.86, 100.75),
            // Perlis
            new Zone("PLS01", "Perlis", "pls-0", "Seluruh Perlis (Kangar, Arau, Padang Besar, Kuala Perlis)", 6.44, 100.2),
            // Pulau Pinang
            new Zone("PNG01", "Pulau Pinang", "png-0", "Seluruh Pulau Pinang (Georgetown, Butterworth)", 5.41, 100.33),
            // Sabah
            new Zone("SBH01", "Sabah", "sbh-1", "Sandakan, Kinabatangan, Bukit Garam", 5.84, 118.12),
            new Zone("SBH02", "Sabah", "sbh-3", "Beluran, Telupid, Kuamut", 5.9, 117.42),
            new Zone("SBH03", "Sabah", "sbh-14", "Lahad Datu, Kunak, Semporna, Tungku", 5.03, 118.34),
            new Zone("SBH04", "Sabah", "sbh-5", "Tawau, Kalabakan, Merotai", 4.24, 117.89),
            new Zone("SBH05", "Sabah", "sbh-9", "Kudat, Kota Marudu, Pitas, Pulau Banggi", 6.88, 116.84),
            new Zone("SBH06", "Sabah", "sbh-4", "Gunung Kinabalu", 6.07, 116.56),
            new Zone("SBH07", "Sabah", "sbh-7", "Kota Kinabalu, Papar, Penampang, Tuaran, Ranau, Kota Belud", 5.98, 116.07),
            new Zone("SBH08", "Sabah", "sbh-6", "Keningau, Tambunan, Nabawan, Pensiangan", 5.34, 116.16),
            new Zone("SBH09", "Sabah", "sbh-2", "Beaufort, Sipitang, Tenom, Kuala Penyu, Weston", 5.35, 115.55),
            // Sarawak
            new Zone("SWK01", "Sarawak", "swk-19", "Limbang, Lawas, Sundar", 4.75, 115.0),
            new Zone("SWK02", "Sarawak", "swk-1", "Miri, Marudi, Niah, Bekenu, Sibuti", 4.39, 113.99),
            new Zone("SWK03", "Sarawak", "swk-2", "Bintulu, Belaga, Tatau, Sebauh", 3.17, 113.04),
            new Zone("SWK04", "Sarawak", "swk-8", "Sibu, Mukah, Kapit, Kanowit, Dalat, Song", 2.29, 111.83),
            new Zone("SWK05", "Sarawak", "swk-13", "Sarikei, Bintangor, Daro, Matu, Julau", 2.13, 111.52),
            new Zone("SWK06", "Sarawak", "swk-11", "Sri Aman, Betong, Saratok, Lubok Antu, Spaoh", 1.24, 111.46),
            new Zone("SWK07", "Sarawak", "swk-24", "Kota Samarahan, Serian, Simunjan, Asajaya, Sebuyau", 1.46, 110.45),
            new Zone("SWK08", "Sarawak", "swk-0", "Kuching, Bau, Lundu, Sematan, Siburan", 1.55, 110.36),
            // Selangor
            new Zone("SGR01", "Selangor", "sgr-1", "Gombak, Hulu Langat, Hulu Selangor, Petaling, Sepang, Shah Alam, Rawang", 3.07, 101.7),
            new Zone("SGR02", "Selangor", "sgr-8", "Sabak Bernam, Kuala Selangor, Sekinchan, Tanjung Karang, Sungai Besar", 3.56, 101.1),
            new Zone("SGR03", "Selangor", "sgr-4", "Klang, Kuala Langat, Banting, Port Klang, Jugra, Morib", 3.0, 101.45),
            // Terengganu
            new Zone("TRG01", "Terengganu", "trg-4", "Kuala Terengganu, Marang", 5.33, 103.14),
            new Zone("TRG02", "Terengganu", "trg-0", "Besut, Setiu, Jerteh, Permaisuri, Kuala Besut", 5.73, 102.5),
            new Zone("TRG03", "Terengganu", "trg-2", "Hulu Terengganu, Kuala Berang, Ajil", 5.07, 103.0),
            new Zone("TRG04", "Terengganu", "trg-3", "Dungun, Kemaman, Kerteh, Paka, Chukai, Kijal", 4.75, 103.42),
            // Wilayah Persekutuan
            new Zone("WLY01", "Wilayah Persekutuan", "wlp-0", "Kuala Lumpur, Putrajaya", 3.14, 101.69),
            new Zone("WLY02", "Wilayah Persekutuan", "wlp-1", "Labuan", 5.28, 115.24),
    };

    /** The default zone before anything is chosen: Kuala Lumpur & Putrajaya. */
    public static final String DEFAULT_MPT = "wlp-0";

    public static Zone find(String mpt) {
        for (Zone z : ZONES) {
            if (z.mpt.equals(mpt)) return z;
        }
        return null;
    }

    private Zones() {
    }
}
