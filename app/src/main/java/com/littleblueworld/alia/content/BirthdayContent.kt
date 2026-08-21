package com.littleblueworld.alia.content

/**
 * Frozen recipient-facing copy from docs/CONTENT.md.
 *
 * Keeping it in one immutable object prevents scene implementations from silently rewriting or
 * duplicating personal text as later milestones are added.
 */
class BirthdayContent {
    val recipientFullName = "Pradipa Fauziyyah Alya"
    val recipientDisplayName = "Alia"
    val birthday = "23 July"
    val authorNamePlaceholder = "[AUTHOR_NAME]"

    val loadingPixels = listOf(
        "waking up the pixels...",
        "pixelating...",
        "checking tiny squares...",
        "assembling something small...",
        "getting the pixels in order...",
        "turning on the tiny things...",
        "putting everything where it belongs...",
        "counting pixels...",
        "recounting pixels...",
        "okay, the pixels are awake.",
    )

    val loadingOcean = listOf(
        "finding the right shade of blue...",
        "tide-setting...",
        "making the ocean extra blue...",
        "putting the clouds in place...",
        "checking the horizon...",
        "moving the waves a little...",
        "making sure the ocean behaves...",
        "adding approximately enough blue...",
        "no, bluer than that...",
        "okay. blue enough.",
    )

    val loadingOceanGrouped = listOf(
        "making the ocean bluer...",
        "no, bluer than that...",
        "okay, perfect.",
    )

    val loadingColor = listOf(
        "adding a little pink...",
        "pinkifying...",
        "lavendering...",
        "okay, maybe some purple too...",
        "making things slightly prettier...",
        "adding exactly three unnecessary sparkles...",
        "checking the pink levels...",
        "maybe a little more pink...",
        "okay that's enough pink.",
        "sprinkling some lavender around...",
    )

    val loadingPersonal = listOf(
        "sorting through pretty things...",
        "saving a few favorites...",
        "brewing imaginary tea...",
        "preparing something for you...",
        "looking for something pretty...",
        "collecting little things...",
        "setting aside the good stuff...",
        "hiding something somewhere...",
        "keeping a few things for later...",
        "making this suspiciously specific...",
    )

    val loadingSignatureStart = "looking for something pretty..."
    val loadingSignatureResult = "found it."

    val loadingBirthday = listOf(
        "putting the stars in place...",
        "wishcrafting...",
        "hiding the surprise...",
        "almost sparkling...",
        "one last check...",
        "making sure nothing fell into the ocean...",
        "saving one star for later...",
        "preparing one little world...",
        "almost there...",
        "okay. this should work.",
    )

    val firstRunFinalLine = "ready."

    val revisitPreFinale = listOf(
        "oh, you're back.",
        "waking the ocean up...",
        "rearranging a few pixels...",
        "putting everything back where you left it...",
        "checking if the fish is still here...",
        "yep.",
        "the little world is still here.",
        "welcome back, alia.",
    )

    val revisitPostFinale = listOf(
        "oh, you're back.",
        "the stars remembered you.",
        "the ocean's still here.",
        "waking the moon up...",
        "checking on the fish...",
        "the fish is pretending not to notice.",
        "everything seems to be where you left it.",
        "welcome back, alia.",
    )

    val birthdayHeading = "HAPPY BIRTHDAY,"
    val birthdayName = "ALIA."
    val birthdayCta = "open your little world"

    val worldHint = listOf(
        "there are a few things hiding around here.",
        "tap anything that looks suspicious.",
    )

    val wishLockedVariants = listOf(
        listOf("not yet.", "go wander around a little."),
        listOf("there's still something to find first."),
        listOf("hmm.", "this one isn't ready yet."),
    )
    val wishUnlock = "something changed."

    val archiveTitle = "ALIA ARCHIVE"
    val archiveSubtitle = "a highly unnecessary collection of good pictures."
    val archiveCaptions = listOf(
        "okay this one's actually unfair.",
        "very alia of her.",
        "no particular reason.\njust keeping this here.",
        "this deserved its own slot.",
        "yeah, this one's staying.",
        "you looked really pretty here.",
    )
    val archiveBackupCaptions = listOf(
        "certified good picture.",
        "somehow elegant and silly at the same time.",
        "this one survived the selection process.",
        "no notes.",
        "one of my favorites.",
        "very rude of this picture to be this good.",
    )

    val bottleMessages = listOf(
        """
        another year unlocked.

        hopefully this one comes with
        more good days,

        less unnecessary problems,

        and significantly better luck.
        """.trimIndent(),
        """
        stay cheerful.

        stay curious.

        stay a little ridiculous.

        basically,

        stay very alia.
        """.trimIndent(),
        """
        i hope life keeps
        being gentle with you.

        and whenever it isn't,

        i hope you always find
        your way back to the things
        that make you smile.
        """.trimIndent(),
        """
        also,

        please continue
        being pretty.

        it would be weird
        to suddenly stop now.
        """.trimIndent(),
    )

    val starsCompletion = listOf(
        "you found all three.",
        "good job, apparently.",
    )
    val starsCta = "back to the ocean"
    val energyTitle = "YOUR ENERGY"
    val energyBody = """
        cheerful without
        trying too hard,

        funny in the most
        random moments,

        and somehow always
        very alia.
    """.trimIndent()
    val mindTitle = "YOUR MIND"
    val mindBody = """
        i like how open you are

        to things,
        people,
        ideas,
        and conversations.

        talking to you
        rarely feels boring.
    """.trimIndent()
    val faceReveal = listOf(
        "YOUR FACE",
        "...",
        "yeah.",
        "you're pretty.",
        "moving on.",
    )

    val cafeHeader = "TODAY'S FORECAST"
    val cafeBody = """
        clear skies

        nice tea

        good coffee

        empty roads

        chance of unnecessary problems:

        hopefully 0%
    """.trimIndent()
    val teaResponses = listOf(
        "good choice.",
        "yes, it's imaginary.",
        "still good tea.",
        "you're committed to this.",
    )
    val coffeeResponses = listOf(
        "also acceptable.",
        "still counts.",
        "respectable decision.",
        "the café approves.",
    )

    val shellMessage = listOf(
        "you found the shell.",
        "there was absolutely no reason to hide this.",
        "but now you have it.",
    )
    val shellOptionalJoke = "congratulations, i guess."

    val fishFirstSequence = listOf(
        "blub.",
        "blub blub.",
        "why are you still tapping me",
        "i'm literally just a fish.",
        "fine.\nhappy birthday, alia.",
    )
    val fishPostFinale = listOf(
        "you again.",
        "yes,\ni remember you.",
        "still just a fish though.",
        "please manage your expectations.",
        "blub.",
    )
    val fishGenericLater = listOf(
        "blub.",
        "busy swimming.",
        "nothing new here.",
        "still a fish.",
        "hello again.",
        "i have no updates.",
    )

    val authorTitle = "ABOUT THE AUTHOR"
    val authorStats = listOf(
        ContentStat("role:", "guy who somehow thought making an apk was reasonable"),
        ContentStat("project duration:", "longer than expected"),
        ContentStat("sleep sacrificed:", "classified"),
        ContentStat("design decisions:", "questionable"),
        ContentStat("intentions:", "good"),
    )
    val authorNote = """
        habede alia.

        maap belum sempet bikin
        hadiah fisik,

        jadi untuk sementara
        aku buatin yang digital dulu.

        it's definitely not perfect,

        but i did spend
        a few nights putting
        this little thing together.

        so...

        hopefully you like it.
    """.trimIndent()

    val wishEntry = listOf("one last thing.", "make a wish.")
    val wishPrompt = "what are you wishing for?"
    val wishPlaceholder = "type something here..."
    val wishSealCta = "hold to seal my wish"
    val wishEmpty = "you need a wish first."
    val wishDestination = "where should this wish go?"
    val wishKeepTitle = "KEEP IT WITH ME"
    val wishKeepDescription = "this wish stays on this device."
    val wishSendTitle = "SEND IT INTO THE LITTLE WORLD"
    val wishSendDescription = "the person who made this can see it."
    val wishSendClearerDescription =
        "this sends your wish to the person who made this little world."
    val wishKeptLocal = "safe with you."
    val wishSending = "sealing your wish..."
    val wishSuccess = listOf(
        "wish sent.",
        "maybe someone can make a tiny part of it come true.",
    )
    val wishOffline = """
        the ocean seems
        a little sleepy.

        i'll keep your wish safe

        and send it
        when it's awake.
    """.trimIndent()
    val wishTemporaryFailure = """
        your wish
        is still safe here.

        we'll try again later.
    """.trimIndent()
    val wishBackgroundSuccess = "your wish found its way."
    val wishAlreadyCompleted = "some wishes only need to be made once."
    val wishLeaveDraft = "leave your wish here for now?"
    val wishLeaveOptions = listOf("keep writing", "leave for now")

    val finalBirthdayMessage = """
        happy birthday, alia.

        i hope this new chapter
        brings you more reasons to laugh,

        more things
        worth looking forward to,

        and plenty of small moments
        that make ordinary days
        feel nice.

        keep being cheerful,

        curious,

        graceful,

        random,

        funny,

        and unmistakably you.

        may life continue
        to treat you

        with all the softness
        and kindness you deserve.
    """.trimIndent()
    val postscript = listOf(
        "p.s.",
        "yes,",
        "i actually made\nan app for this.",
    )
    val postscriptCta = "there's one more thing"
    val ending = listOf("okay.", "that's actually it.", "probably.")
    val endingCta = "wander around again"

    val soundOn = "sound on"
    val soundOff = "sound off"

    val accessibilityLabels = listOf(
        "Open Alia Archive",
        "Open message bottles",
        "Open collectible stars",
        "Open Tiny Café",
        "Explore shell",
        "Tiny blue fish",
        "Open Make a Wish",
        "Open About the Author",
        "Back",
        "Turn sound on / Turn sound off",
    )

    fun authorSignature(authorName: String = authorNamePlaceholder): String = "— $authorName"

    fun photoAccessibilityLabel(index: Int): String {
        require(index in 1..ARCHIVE_PHOTO_COUNT)
        return "Photo of Alia, $index of $ARCHIVE_PHOTO_COUNT"
    }

    data class ContentStat(
        val label: String,
        val value: String,
    )

    companion object {
        const val ARCHIVE_PHOTO_COUNT = 6
        const val MAX_WISH_LENGTH = 500
    }
}
