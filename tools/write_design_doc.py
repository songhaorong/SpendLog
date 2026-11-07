#!/usr/bin/env python3
"""Write the Milestone 1 design document as a .docx without extra libraries."""

import zipfile
from xml.sax.saxutils import escape

OUT = "/Users/song/Desktop/作业2/SpendLog_Milestone1_Design.docx"

PARAS = [
    ("title", "SpendLog"),
    ("subtitle", "Milestone 1 design documentation"),
    ("meta", "Name: Haorong Song"),
    ("meta", "Student ID: 3184339"),
    ("meta", "Module: BSC-MD, Developing an Android App"),
    ("meta", "Date: 7 November 2026"),
    ("h", "1. Main idea"),
    (
        "p",
        "SpendLog is a small phone app for writing down money as it leaves. Coffee, lunch, the bus, Tesco, Lidl, the Asian market. By the end of a week it is hard to remember where the money went, so the entry is added at the moment of paying. Each entry has a name, an amount in euro, a category, a date and a short note. It also stores the location from that moment, so a row that only says food and €8 can still mean lunch around college rather than a number with no place attached.",
    ),
    (
        "p",
        "There is no login, no chart and no budget. The app is a list that can be added to, opened, corrected and deleted. That is the whole product for this milestone. The proposal already set that boundary, because the module only gives a few weeks, and Milestone 1 asks for the basic design and the basic behaviour.",
    ),
    ("h", "2. Objectives"),
    (
        "p",
        "The practical objective is to record a payment in under a minute, see what the current month has come to, and open one payment later if the amount or the note needs fixing.",
    ),
    (
        "p",
        "The marking objective is the one this build is built around. There are three layouts that do different jobs. The user can actually operate them: type, choose, save, open, edit and delete. Moving between screens inside the app uses an explicit intent, because the destination class is known. Leaving the app to show the place on a map uses an implicit intent, because any maps application that understands a geo uri is acceptable. Location comes from the phone's location sensor and is read once, on save. If permission is refused, the payment is still stored.",
    ),
    ("h", "3. What the three screens do"),
    (
        "p",
        "Home is MainActivity, and it is the screen that opens. The top card says Spent this month and shows the total of every row whose date falls in the current calendar month. The month name sits under the label so it is obvious which month the figure belongs to. The add button sits on that card, as in the proposal sketch. Under the card is the list, newest first. The name is on the left, the amount is on the right, and the category and date sit under the name so a row can be recognised without opening it. Tapping a row opens that expense. An empty database says so, instead of showing a blank page.",
    ),
    (
        "p",
        "Add and edit share AddExpenseActivity. One form is enough, and the title changes. A new expense starts with today's date and the category Food. Editing loads the chosen row into the same fields. The fields are the name, the amount, the category, the date and the note. The category is a fixed list: Food, Transport, Shopping, Other. Custom categories are not part of this version. The date is chosen from a date picker rather than typed, and the field shows it as 7 Nov 2026. The stored value is an ISO date. Save is at the bottom. The name cannot be blank, and the amount has to be greater than zero. While a save is in progress the button is disabled, so a double tap does not insert two rows.",
    ),
    (
        "p",
        "Detail is ExpenseDetailActivity. It shows one expense: name, amount, category, date, note and location. Location prints the latitude and longitude, or Not saved when the row has none. Under the facts are Edit, Delete and Open in Maps. Delete asks first. The dialog names the expense and waits for Delete or Cancel, because a fast tap on the wrong row should not remove it.",
    ),
    ("h", "4. How the screens are built"),
    (
        "p",
        "The interface is Jetpack Compose and the language is Kotlin. The three pages are still three activities. They are not three composables swapped inside a single activity, because then an explicit intent would not be starting an activity. Each activity calls setContent with its own composable, so the layouts stay separate: a summary and a list, a form, and a read-only record with actions.",
    ),
    (
        "p",
        "The colours are a warm paper background, a dark green for the month card and the amounts, and a white card for each row and for the detail facts. The add button on the green card is white, so it does not disappear into the card. Home is a card plus a scrolling list. The form is a vertical stack of fields. Detail is a large amount, a block of labelled facts, and three actions. The three layouts are not the same screen with different text.",
    ),
    ("h", "5. Explicit intents"),
    (
        "p",
        "Both destinations belong to this app, so the intent names the class and Android does not have to resolve an action against other applications.",
    ),
    (
        "p",
        "The add button builds an Intent aimed at AddExpenseActivity and starts it. There is no extra. The form treats a missing id as a new row. Tapping a row builds an Intent aimed at ExpenseDetailActivity and puts the expense id in an extra named expense_id. Detail closes immediately if that extra is missing or the row has already been deleted, so it cannot sit on an empty page. Edit, on the detail page, builds an Intent aimed at AddExpenseActivity with the same extra. The form loads that row, and Save writes over it instead of inserting another one.",
    ),
    (
        "p",
        "None of these intents set an action that another app could claim. AddExpenseActivity and ExpenseDetailActivity are not exported. Only the launcher activity is exported, because it has to be.",
    ),
    ("h", "6. Implicit intent"),
    (
        "p",
        "Open in Maps does not name a package. It uses ACTION_VIEW and a geo uri built from the stored latitude and longitude, with the expense name as the query label. Android offers whatever application can handle that uri. The manifest declares a queries element for ACTION_VIEW on the geo scheme, because from Android 11 an app cannot see other handlers unless it says it intends to look. If nothing can handle the uri, startActivity throws ActivityNotFoundException and the app shows the toast no maps app.",
    ),
    (
        "p",
        "If the row has no coordinates, the button does not fire the intent. It shows No location saved for this expense. Open in Maps does not run for a row that never stored a place.",
    ),
    ("h", "7. Sensor"),
    (
        "p",
        "The sensor is the device location, preferably GPS. The app is not about the sensor. The reading exists so an expense can remember where it was added, and so Open in Maps has coordinates. It is read once, when Save is pressed, not on a timer and not in the background.",
    ),
    (
        "p",
        "The first save asks for fine and coarse location. If the answer is no, the insert or update still happens and the new coordinates stay empty. On an edit, a refused or failed reading keeps the coordinates already stored, so a later edit does not wipe a place that was saved earlier.",
    ),
    (
        "p",
        "When permission is already granted, a fix from the last two minutes is reused. Waiting on a cold GPS start for a place the phone already knows does not add anything. Otherwise the app asks LocationManager for one current fix: GPS when fine permission is granted and that provider is on, otherwise the network provider. If that does not return within eight seconds, the last known fix is used. If there is still nothing, the row is saved without a location and the toast says so. The toast is Saved when a new fix was written, Saved. Location unchanged. when an edit kept the old coordinates, and Saved without a location when the row has none.",
    ),
    ("h", "8. Storage"),
    (
        "p",
        "The rows stay on the phone in SQLite. The database file is spendlog.db and the only table is expenses. There is no account and nothing is uploaded. A row holds the id, the name, the amount, the category, the date as yyyy-MM-dd, the note, and the latitude and longitude. The two coordinates can be null.",
    ),
    (
        "p",
        "Saving a new expense inserts a row. Home loads every row, newest date first and then highest id, and Detail loads the one row for the id that arrived with the intent. The month total is not a second table. It is the sum of the loaded rows whose date is in the current month. Saving an edit writes over that same row. Delete removes the row after the confirmation.",
    ),
    (
        "p",
        "The first time the database is created it inserts five sample rows, dated inside the current month, so the list and the total can be shown immediately. Coffee keeps the sample coordinates 53.33, -6.28. Bus ticket is stored with no location, so both branches of Open in Maps can be demonstrated. Sample rows are inserted only in onCreate. Deleting them does not bring them back.",
    ),
    ("h", "9. What this build covers"),
    (
        "p",
        "Milestone 1 asked for the basic design and the basic behaviour: three distinctive functional layouts, user interaction, and both kinds of intent. Home, the form and the detail page are those layouts. Typing, picking a category, changing the date, saving, opening, editing and deleting are the interaction. The class-named intents are the explicit ones. Open in Maps is the implicit one. The location permission and the single read on save are the sensor.",
    ),
    (
        "p",
        "Charts, a budget, custom categories and any kind of account are out of scope. They were out of scope in the proposal, and they are still out. Later work can add to this without replacing the three activities, because the list, the form and the record are already the core.",
    ),
    ("h", "10. What the demonstration should show"),
    (
        "p",
        "Open the SpendLog project in Android Studio and run it on a phone or an emulator. On an emulator, set a location in the extended controls before saving a new row, and allow the location permission when it appears. Show the month total and the list. Add a row. Open it. Edit it. Delete one row and cancel the dialog, then delete it for real. Open Coffee in Maps. Open Bus ticket and show the toast, because that sample has no location. Deny permission once, on a new row, and show that the row still exists with location Not saved.",
    ),
]


def paragraph(style, text):
    text = escape(text)
    if style == "title":
        props = '<w:pPr><w:spacing w:before="0" w:after="80"/><w:jc w:val="left"/></w:pPr>'
        run = (
            '<w:r><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>'
            '<w:b/><w:sz w:val="40"/><w:szCs w:val="40"/>'
            '<w:color w:val="1F4D3A"/></w:rPr>'
            f"<w:t>{text}</w:t></w:r>"
        )
    elif style == "subtitle":
        props = '<w:pPr><w:spacing w:before="0" w:after="200"/></w:pPr>'
        run = (
            '<w:r><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>'
            '<w:sz w:val="28"/><w:szCs w:val="28"/>'
            '<w:color w:val="1C1A16"/></w:rPr>'
            f"<w:t>{text}</w:t></w:r>"
        )
    elif style == "meta":
        props = '<w:pPr><w:spacing w:before="0" w:after="40"/></w:pPr>'
        run = (
            '<w:r><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>'
            '<w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr>'
            f"<w:t>{text}</w:t></w:r>"
        )
    elif style == "h":
        props = '<w:pPr><w:spacing w:before="280" w:after="80"/></w:pPr>'
        run = (
            '<w:r><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>'
            '<w:b/><w:sz w:val="28"/><w:szCs w:val="28"/>'
            '<w:color w:val="1F4D3A"/></w:rPr>'
            f"<w:t>{text}</w:t></w:r>"
        )
    else:
        props = (
            '<w:pPr><w:spacing w:before="60" w:after="80" w:line="276" w:lineRule="auto"/>'
            '<w:jc w:val="both"/></w:pPr>'
        )
        run = (
            '<w:r><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>'
            '<w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr>'
            f"<w:t xml:space=\"preserve\">{text}</w:t></w:r>"
        )
    return f"<w:p>{props}{run}</w:p>"


body = "".join(paragraph(kind, text) for kind, text in PARAS)
sect = (
    "<w:sectPr>"
    '<w:pgSz w:w="11906" w:h="16838"/>'
    '<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" '
    'w:header="720" w:footer="720" w:gutter="0"/>'
    "</w:sectPr>"
)
document = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
    f"<w:body>{body}{sect}</w:body></w:document>"
)

content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>
"""

rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>
"""

with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED) as zf:
    zf.writestr("[Content_Types].xml", content_types)
    zf.writestr("_rels/.rels", rels)
    zf.writestr("word/document.xml", document)

words = sum(len(text.split()) for _, text in PARAS)
print(f"wrote {OUT}")
print(f"paragraphs={len(PARAS)} words={words}")
