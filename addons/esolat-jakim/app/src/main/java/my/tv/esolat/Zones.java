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

        Zone(String jakim, String state, String mpt, String desc) {
            this.jakim = jakim;
            this.state = state;
            this.mpt = mpt;
            this.desc = desc;
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
            new Zone("JHR01", "Johor", "jhr-9", "Pulau Aur & Pemanggil"),
            new Zone("JHR02", "Johor", "jhr-2", "Johor Bahru, Kota Tinggi, Mersing, Kulai, Pasir Gudang"),
            new Zone("JHR03", "Johor", "jhr-3", "Kluang, Pontian, Simpang Renggam, Benut"),
            new Zone("JHR04", "Johor", "jhr-0", "Batu Pahat, Muar, Segamat, Gemas, Yong Peng"),
            // Kedah
            new Zone("KDH01", "Kedah", "kdh-2", "Kota Setar, Kubang Pasu, Pokok Sena (Alor Setar, Jitra)"),
            new Zone("KDH02", "Kedah", "kdh-12", "Yan, Kuala Muda, Pendang (Sungai Petani)"),
            new Zone("KDH03", "Kedah", "kdh-11", "Sik, Padang Terap, Kuala Nerang"),
            new Zone("KDH04", "Kedah", "kdh-0", "Baling, Kuala Ketil"),
            new Zone("KDH05", "Kedah", "kdh-5", "Kulim, Bandar Baharu, Serdang, Lunas, Padang Serai"),
            new Zone("KDH06", "Kedah", "kdh-6", "Langkawi"),
            new Zone("KDH07", "Kedah", "kdh-10", "Puncak Gunung Jerai"),
            // Kelantan (one zone: JAKIM merged the old KTN02/KTN03 into KTN01)
            new Zone("KTN01", "Kelantan", "ktn-0", "Seluruh Kelantan (Kota Bharu, Pasir Mas, Kuala Krai, Machang, Tumpat)"),
            // Melaka
            new Zone("MLK01", "Melaka", "mlk-1", "Bandar Melaka, Alor Gajah, Jasin, Masjid Tanah, Merlimau"),
            // Negeri Sembilan
            new Zone("NGS01", "Negeri Sembilan", "ngs-1", "Jempol, Tampin, Bahau, Gemencheh"),
            new Zone("NGS02", "Negeri Sembilan", "ngs-4", "Seremban, Port Dickson, Nilai, Rembau, Kuala Pilah, Jelebu"),
            // Pahang
            new Zone("PHG01", "Pahang", "phg-12", "Pulau Tioman"),
            new Zone("PHG02", "Pahang", "phg-8", "Kuantan, Pekan, Rompin, Muadzam Shah, Gambang"),
            new Zone("PHG03", "Pahang", "phg-1", "Temerloh, Maran, Jerantut, Bera, Jengka"),
            new Zone("PHG04", "Pahang", "phg-7", "Bentong, Raub, Kuala Lipis, Mentakab, Lanchang"),
            new Zone("PHG05", "Pahang", "phg-17", "Janda Baik, Genting Sempah, Bukit Tinggi"),
            new Zone("PHG06", "Pahang", "phg-3", "Cameron Highlands, Bukit Fraser, Genting Highlands"),
            // Perak
            new Zone("PRK01", "Perak", "prk-24", "Tapah, Slim River, Tanjung Malim, Sungkai, Bidor"),
            new Zone("PRK02", "Perak", "prk-2", "Ipoh, Batu Gajah, Kampar, Kuala Kangsar, Sungai Siput"),
            new Zone("PRK03", "Perak", "prk-15", "Grik, Lenggong, Pengkalan Hulu"),
            new Zone("PRK04", "Perak", "prk-3", "Belum, Temengor"),
            new Zone("PRK05", "Perak", "prk-0", "Lumut, Sitiawan, Teluk Intan, Manjung, Bagan Datoh"),
            new Zone("PRK06", "Perak", "prk-1", "Taiping, Parit Buntar, Bagan Serai, Selama, Kamunting"),
            new Zone("PRK07", "Perak", "prk-5", "Bukit Larut (Maxwell Hill)"),
            // Perlis
            new Zone("PLS01", "Perlis", "pls-0", "Seluruh Perlis (Kangar, Arau, Padang Besar, Kuala Perlis)"),
            // Pulau Pinang
            new Zone("PNG01", "Pulau Pinang", "png-0", "Seluruh Pulau Pinang (Georgetown, Butterworth)"),
            // Sabah
            new Zone("SBH01", "Sabah", "sbh-1", "Sandakan, Kinabatangan, Bukit Garam"),
            new Zone("SBH02", "Sabah", "sbh-3", "Beluran, Telupid, Kuamut"),
            new Zone("SBH03", "Sabah", "sbh-14", "Lahad Datu, Kunak, Semporna, Tungku"),
            new Zone("SBH04", "Sabah", "sbh-5", "Tawau, Kalabakan, Merotai"),
            new Zone("SBH05", "Sabah", "sbh-9", "Kudat, Kota Marudu, Pitas, Pulau Banggi"),
            new Zone("SBH06", "Sabah", "sbh-4", "Gunung Kinabalu"),
            new Zone("SBH07", "Sabah", "sbh-7", "Kota Kinabalu, Papar, Penampang, Tuaran, Ranau, Kota Belud"),
            new Zone("SBH08", "Sabah", "sbh-6", "Keningau, Tambunan, Nabawan, Pensiangan"),
            new Zone("SBH09", "Sabah", "sbh-2", "Beaufort, Sipitang, Tenom, Kuala Penyu, Weston"),
            // Sarawak
            new Zone("SWK01", "Sarawak", "swk-19", "Limbang, Lawas, Sundar"),
            new Zone("SWK02", "Sarawak", "swk-1", "Miri, Marudi, Niah, Bekenu, Sibuti"),
            new Zone("SWK03", "Sarawak", "swk-2", "Bintulu, Belaga, Tatau, Sebauh"),
            new Zone("SWK04", "Sarawak", "swk-8", "Sibu, Mukah, Kapit, Kanowit, Dalat, Song"),
            new Zone("SWK05", "Sarawak", "swk-13", "Sarikei, Bintangor, Daro, Matu, Julau"),
            new Zone("SWK06", "Sarawak", "swk-11", "Sri Aman, Betong, Saratok, Lubok Antu, Spaoh"),
            new Zone("SWK07", "Sarawak", "swk-24", "Kota Samarahan, Serian, Simunjan, Asajaya, Sebuyau"),
            new Zone("SWK08", "Sarawak", "swk-0", "Kuching, Bau, Lundu, Sematan, Siburan"),
            // Selangor
            new Zone("SGR01", "Selangor", "sgr-1", "Gombak, Hulu Langat, Hulu Selangor, Petaling, Sepang, Shah Alam, Rawang"),
            new Zone("SGR02", "Selangor", "sgr-8", "Sabak Bernam, Kuala Selangor, Sekinchan, Tanjung Karang, Sungai Besar"),
            new Zone("SGR03", "Selangor", "sgr-4", "Klang, Kuala Langat, Banting, Port Klang, Jugra, Morib"),
            // Terengganu
            new Zone("TRG01", "Terengganu", "trg-4", "Kuala Terengganu, Marang"),
            new Zone("TRG02", "Terengganu", "trg-0", "Besut, Setiu, Jerteh, Permaisuri, Kuala Besut"),
            new Zone("TRG03", "Terengganu", "trg-2", "Hulu Terengganu, Kuala Berang, Ajil"),
            new Zone("TRG04", "Terengganu", "trg-3", "Dungun, Kemaman, Kerteh, Paka, Chukai, Kijal"),
            // Wilayah Persekutuan
            new Zone("WLY01", "Wilayah Persekutuan", "wlp-0", "Kuala Lumpur, Putrajaya"),
            new Zone("WLY02", "Wilayah Persekutuan", "wlp-1", "Labuan"),
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
