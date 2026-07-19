package com.sangeetsetu.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.sangeetsetu.app.AppSettings

interface LanguageStrings {
    val appName: String
    val home: String
    val categories: String
    val bookings: String
    val chat: String
    val profile: String
    val greetings: String
    val selectBestArtist: String
    val vipArtists: String
    val mostBooked: String
    val premiumMembers: String
    val liveStatus: String
    val popularSearches: String
    val recentlyAdded: String
    val trendingArtists: String
    val recommendedForYou: String
    val topRated: String
    val editorsChoice: String
    val liveEvents: String
    val eventTimeline: String
    val seeAll: String
    val bookNow: String
    val tickets: String
    val festivalSpecial: String
    val goldMembership: String
    val limitedOffer: String
    val whyChooseUs: String
    val verifiedArtists: String
    val securePayment: String
    val fastBooking: String
    val support247: String
    val userReviews: String
    val needHelp: String
    val contactExpert: String
    val language: String
    val theme: String
    val notifications: String
    val searchPlaceholder: String
    val selectLocation: String
    val online: String
    val typing: String
    val activeNow: String
    val details: String
    val viewProfile: String
    val districtSelector: String
    val artistSelector: String
    val heroSubtitle: String
    val heroDescription: String
    val trendingNow: String
    val festiveSpecial: String
    val liveExperience: String
    val vipExperience: String
    val risingStars: String
    val communityFavorites: String
    val specialGoldOffer: String
    val goldOfferSub: String
    val shareWithFriends: String
    val freePremiumId: String
    val referralDesc: String
    val shareNow: String
    val progress: String
    val register: String
    val registrationTitle: String
    val registrationSubtitle: String
    val submitRegistration: String
    val selectState: String
    val selectDistrict: String
    val uploadPhoto: String
    val uploadVideo: String
    val uploadAudio: String
    val uploadFile: String
    val termsAgreement: String
    val validationRequired: String
    val validationInvalidFormat: String
    val searchOptions: String
    val guest: String
    val today: String
    val tomorrow: String
    val thisWeek: String
    val nextMonth: String
    val live: String
    val fewSeatsLeft: String
    val venueCenter: String
    val yearsExp: String
    val contactUs: String
    val helplineLabel: String
    val about: String
    val privacy: String
    val terms: String
    val support: String
    val madeInBharat: String
}

object EnglishStrings : LanguageStrings {
    override val appName = "Sangeet Setu"
    override val home = "Home"
    override val categories = "Categories"
    override val bookings = "Bookings"
    override val chat = "Chat"
    override val profile = "Profile"
    override val greetings = "Hello"
    override val selectBestArtist = "Choose Best Artist"
    override val vipArtists = "VIP Artists"
    override val mostBooked = "Most Booked"
    override val premiumMembers = "Premium Members"
    override val liveStatus = "Live Booking Status"
    override val popularSearches = "Popular Searches"
    override val recentlyAdded = "Recently Added"
    override val trendingArtists = "Trending Artists"
    override val recommendedForYou = "Recommended for You"
    override val topRated = "Top Rated Artists"
    override val editorsChoice = "Editor's Choice"
    override val liveEvents = "Live Events"
    override val eventTimeline = "Event Timeline"
    override val seeAll = "See All"
    override val bookNow = "Book Now"
    override val tickets = "Book Tickets"
    override val festivalSpecial = "Upcoming Festivals"
    override val goldMembership = "Gold Membership"
    override val limitedOffer = "LIMITED OFFER"
    override val whyChooseUs = "Why Choose Sangeet Setu?"
    override val verifiedArtists = "Verified Artists"
    override val securePayment = "Secure Payment"
    override val fastBooking = "Fast Booking"
    override val support247 = "24/7 Support"
    override val userReviews = "User Reviews"
    override val needHelp = "Need help with booking?"
    override val contactExpert = "Contact our expert now"
    override val language = "Language"
    override val theme = "Theme"
    override val notifications = "Notifications"
    override val searchPlaceholder = "Search for artists..."
    override val selectLocation = "Select Location"
    override val online = "Online"
    override val typing = "Typing..."
    override val activeNow = "ACTIVE NOW"
    override val details = "Details"
    override val viewProfile = "View Profile"
    override val districtSelector = "Which district's artists do you want to see?"
    override val artistSelector = "Which artist are you looking for?"
    override val heroSubtitle = "Jode Kalakar, Banaye Yaadgar Pal"
    override val heroDescription = "Book top artists and narrators for your events now."
    override val trendingNow = "Trending Now"
    override val festiveSpecial = "Festive Special"
    override val liveExperience = "Live Experience"
    override val vipExperience = "VIP Artist Experience"
    override val risingStars = "Rising Stars"
    override val communityFavorites = "Community Favorites"
    override val specialGoldOffer = "Special Gold Offer"
    override val goldOfferSub = "Get up to 15% off on bookings this month"
    override val shareWithFriends = "🎁 Share with 10 People"
    override val freePremiumId = "Get Verified Badge ✅"
    override val referralDesc = "Share your referral link with 10 people. A Verified Badge will be added to your profile once the condition is met."
    override val shareNow = "Share Now"
    override val progress = "Progress"
    override val register = "Register"
    override val registrationTitle = "Registration Form"
    override val registrationSubtitle = "Join our elite community of artists"
    override val submitRegistration = "SUBMIT REGISTRATION"
    override val selectState = "Select State"
    override val selectDistrict = "Select District"
    override val uploadPhoto = "Upload Photo"
    override val uploadVideo = "Upload Video"
    override val uploadAudio = "Upload Audio"
    override val uploadFile = "Upload File"
    override val termsAgreement = "I agree to the Terms & Conditions and Privacy Policy."
    override val validationRequired = "is required"
    override val validationInvalidFormat = "Invalid format"
    override val searchOptions = "Search options..."
    override val guest = "Guest"
    override val today = "Today"
    override val tomorrow = "Tomorrow"
    override val thisWeek = "This Week"
    override val nextMonth = "Next Month"
    override val live = "LIVE"
    override val fewSeatsLeft = "Few Seats Left"
    override val venueCenter = "Venue Center"
    override val yearsExp = "Years Exp."
    override val contactUs = "Contact Us:"
    override val helplineLabel = "Helpline:"
    override val about = "About"
    override val privacy = "Privacy"
    override val terms = "Terms"
    override val support = "Support"
    override val madeInBharat = "Made with ❤️ in Bharat"
}

object HindiStrings : LanguageStrings {
    override val appName = "संगीत सेतु"
    override val home = "होम"
    override val categories = "श्रेणियां"
    override val bookings = "बुकिंग्स"
    override val chat = "चैट"
    override val profile = "प्रोफ़ाइल"
    override val greetings = "नमस्ते"
    override val selectBestArtist = "श्रेष्ठ कलाकार चुनें"
    override val vipArtists = "VIP कलाकार"
    override val mostBooked = "सर्वाधिक बुक किए गए"
    override val premiumMembers = "प्रीमियम सदस्य"
    override val liveStatus = "लाइव बुकिंग स्टेटस"
    override val popularSearches = "लोकप्रिय खोजें"
    override val recentlyAdded = "हाल ही में जुड़े"
    override val trendingArtists = "ट्रेंडिंग कलाकार"
    override val recommendedForYou = "आपके लिए अनुशंसित"
    override val topRated = "टॉप रेटेड कलाकार"
    override val editorsChoice = "एडिटर की पसंद"
    override val liveEvents = "लाइव इवेंट्स"
    override val eventTimeline = "इवेंट टाइमलाइन"
    override val seeAll = "सभी देखें"
    override val bookNow = "बुक करें"
    override val tickets = "टिकट बुक करें"
    override val festivalSpecial = "आगामी त्यौहार उत्सव"
    override val goldMembership = "गोल्ड मेंबरशिप"
    override val limitedOffer = "सीमित ऑफर"
    override val whyChooseUs = "संगीतमय सेतु क्यों चुनें?"
    override val verifiedArtists = "वेरिफाइड कलाकार"
    override val securePayment = "सुरक्षित भुगतान"
    override val fastBooking = "फास्ट बुकिंग"
    override val support247 = "24/7 सपोर्ट"
    override val userReviews = "यूज़र्स के अनुभव"
    override val needHelp = "बुकिंग में मदद चाहिए?"
    override val contactExpert = "हमारे एक्सपर्ट से संपर्क करें"
    override val language = "भाषा"
    override val theme = "थीम"
    override val notifications = "सूचनाएं"
    override val searchPlaceholder = "कलाकारों की खोज करें..."
    override val selectLocation = "स्थान चुनें"
    override val online = "ऑनलाइन"
    override val typing = "टाइपिंग..."
    override val activeNow = "अभी सक्रिय"
    override val details = "विवरण"
    override val viewProfile = "प्रोफ़ाइल देखें"
    override val districtSelector = "आप किस जिले के कलाकार देखना चाहते हैं?"
    override val artistSelector = "आप कौन सा कलाकार ढूंढना चाहते हैं?"
    override val heroSubtitle = "जोड़ें कलाकार, बनायें यादगार पल"
    override val heroDescription = "अपने कार्यक्रम के लिए श्रेष्ठ कलाकार और कथावाचक अभी बुक करें।"
    override val trendingNow = "अभी ट्रेंडिंग में"
    override val festiveSpecial = "फेस्टिव स्पेशल"
    override val liveExperience = "लाइव अनुभव"
    override val vipExperience = "VIP कलाकार अनुभव"
    override val risingStars = "उभरते सितारे"
    override val communityFavorites = "कम्युनिटी की पसंद"
    override val specialGoldOffer = "विशेष गोल्ड ऑफर"
    override val goldOfferSub = "इस महीने बुकिंग पर पाएं 15% तक की छूट"
    override val shareWithFriends = "10 लोगों के साथ शेयर करें, वेरिफाइड बैज पाएं ✅"
    override val freePremiumId = "वेरिफाइड बैज पाएं ✅"
    override val referralDesc = "अपने रेफ़रल लिंक को 10 लोगों के साथ साझा करें। शर्त पूरी होने पर आपके प्रोफ़ाइल पर Verified Badge जोड़ दिया जाएगा।"
    override val shareNow = "अभी शेयर करें"
    override val progress = "प्रोग्रेस"
    override val register = "रजिस्टर करें"
    override val registrationTitle = "पंजीकरण फॉर्म"
    override val registrationSubtitle = "कलाकारों के हमारे विशिष्ट समुदाय में शामिल हों"
    override val submitRegistration = "पंजीकरण जमा करें"
    override val selectState = "राज्य चुनें"
    override val selectDistrict = "जिला चुनें"
    override val uploadPhoto = "फोटो अपलोड करें"
    override val uploadVideo = "वीडियो अपलोड करें"
    override val uploadAudio = "ऑडियो अपलोड करें"
    override val uploadFile = "फाइल अपलोड करें"
    override val termsAgreement = "मैं नियम और शर्तों और गोपनीयता नीति से सहमत हूं।"
    override val validationRequired = "अनिवार्य है"
    override val validationInvalidFormat = "अमान्य प्रारूप"
    override val searchOptions = "विकल्प खोजें..."
    override val guest = "अतिथि"
    override val today = "आज"
    override val tomorrow = "कल"
    override val thisWeek = "इस सप्ताह"
    override val nextMonth = "अगला महीना"
    override val live = "लाइव"
    override val fewSeatsLeft = "कुछ ही सीटें बची हैं"
    override val venueCenter = "वेन्यू सेंटर"
    override val yearsExp = "वर्षों का अनुभव"
    override val contactUs = "संपर्क करें:"
    override val helplineLabel = "हेल्पलाइन:"
    override val about = "बारे में"
    override val privacy = "गोपनीयता"
    override val terms = "शर्तें"
    override val support = "सहायता"
    override val madeInBharat = "भारत में ❤️ के साथ निर्मित"
}

val LocalStrings = staticCompositionLocalOf<LanguageStrings> { EnglishStrings }

@Composable
fun ProvideStrings(content: @Composable () -> Unit) {
    val currentLanguage = AppSettings.language.value
    val strings = when (currentLanguage) {
        "Hindi" -> HindiStrings
        "English" -> EnglishStrings
        "System" -> {
            val locale = java.util.Locale.getDefault().language
            if (locale == "hi") HindiStrings else EnglishStrings
        }
        else -> EnglishStrings
    }
    CompositionLocalProvider(LocalStrings provides strings) {
        content()
    }
}
