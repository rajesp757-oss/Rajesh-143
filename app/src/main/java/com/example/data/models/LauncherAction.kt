package com.example.data.models

enum class LauncherActionType(
    val title: String,
    val hindiTitle: String,
    val odiaTitle: String,
    val description: String
) {
    PHONE("Phone", "फ़ोन डायल", "ଫୋନ୍ ଡାୟାଲ୍", "Open phone dialer"),
    CAMERA("Camera", "कैमरा", "କ୍ୟାମେରା", "Launch camera"),
    MESSAGES("Messages", "संदेश", "ମେସେଜ୍", "Open messaging app"),
    CONTACTS("Contacts", "संपर्क", "କଣ୍ଟାକ୍ଟସ୍", "Open phone contacts"),
    SETTINGS("Settings", "सेटिंग्स", "ସେଟିଂସ୍", "Open device settings"),
    CLOCK("Alarms & Clock", "घड़ी / अलार्म", "ଘଣ୍ଟା / ଆଲାର୍ମ", "Open clock & alarms"),
    CALCULATOR("Calculator", "कैलकुलेटर", "କାଲକୁଲେଟର୍", "Open calculator"),
    BROWSER("Web Browser", "ब्राउज़र", "ବ୍ରାଉଜର୍", "Open internet browser")
}

data class LauncherAction(
    val type: LauncherActionType,
    val payload: String = ""
)
