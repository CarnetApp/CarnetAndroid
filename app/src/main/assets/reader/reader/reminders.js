var RemindersUtils = function () {

}

/*
    This method translates an absolute UTC timestamp and translates it to locale TS

    16:20 in UK will remain 16:20 in China
*/

RemindersUtils.translateTimeToLocalTime = function (timestamp) {
    var dUtc = new Date(timestamp);
    var d = new Date();
    d.setHours(dUtc.getUTCHours())
    d.setMinutes(dUtc.getUTCMinutes())
    return d.getTime()
}

RemindersUtils.translateLocalTimeToTimestamp = function (time) {
    var dUtc = new Date(time);
    var d = new Date();
    d.setUTCHours(dUtc.getHours())
    d.setUTCMinutes(dUtc.getMinutes())
    return d.getTime()
}

var RemindersDialog = function (element, reminders) {
    this.dialog = element
    this.reminders = reminders
    this.dialog.getElementsByClassName("add")[0].onclick = function () {
        element.close()
        var reminderItemDialog = new ReminderItemDialog(document.getElementById("reminder-item"))
        reminderItemDialog.dialog.showModal()
    }
    this.dialog.getElementsByClassName("close")[0].onclick = function () {
        element.close()
    }
    this.refresh()
}

RemindersDialog.prototype.refresh = function () {
    this.dialog.getElementsByClassName("reminders-container")[0].innerHTML = ""
    if (this.reminders != undefined)
        for (var reminder of this.reminders) {
            this.addItem(reminder)
        }
}

RemindersDialog.prototype.addItem = function (reminder) {
    var dialog = this
    var reminderDiv = document.createElement("div")
    reminderDiv.classList.add("reminder-item")
    var deleteButton = document.createElement("button")
    deleteButton.classList.add("delete-reminder")
    deleteButton.classList.add("mdl-button")
    deleteButton.innerHTML = "<i class=\"material-icons\">delete</i>"

    reminderDiv.appendChild(deleteButton)
    var time = document.createElement("span")
    time.classList.add("hour")

    var d = new Date(reminder.time);

    time.innerHTML = ("0" + d.getUTCHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2);
    reminderDiv.appendChild(time)

    if (reminder.frequency == "days-of-week") {

        for (var day of reminder.days) {
            var dayDiv = document.createElement("span")
            dayDiv.classList.add("day")
            dayDiv.innerHTML = $.i18n(day) + " ";
            reminderDiv.appendChild(dayDiv)
        }
    }
    else {
        var date = document.createElement("span")
        date.classList.add("date")
        d = new Date();
        d.setFullYear(reminder.year)
        d.setDate(reminder.dayOfMonth)
        d.setMonth(reminder.month - 1)
        d.setHours(0)
        d.setMinutes(0)
        d.setSeconds(0)
        date.innerHTML = d.toLocaleDateString();

        var frequency = document.createElement("frequency")
        frequency.classList.add("frequency")
        frequency.innerHTML = $.i18n(reminder.frequency) + " - "
        reminderDiv.appendChild(frequency)
        reminderDiv.appendChild(date)

    }

    var remindersManager = this;

    reminderDiv.onclick = function () {

        var reminderItemDialog = new ReminderItemDialog(document.getElementById("reminder-item"), reminder)
        reminderItemDialog.dialog.showModal()
        remindersManager.dialog.close()
    }

    $(deleteButton).click(function (e) {
        dialog.reminders.splice(dialog.reminders.indexOf(reminder), 1);
        dialog.refresh()
        e.stopPropagation();
        writer.hasTextChanged = true;

    });
    this.dialog.getElementsByClassName("reminders-container")[0].appendChild(reminderDiv)

}


var ReminderItemDialog = function (element, reminder) {
    this.dialog = element;
    this.reminder = reminder;
    if (reminder == undefined) {
        this.reminder = {}
        this.reminder.frequency = "once"
        this.reminder.dayOfMonth = new Date().getDate()
        this.reminder.month = new Date().getMonth() + 1
        this.reminder.year = new Date().getFullYear()
        this.reminder.date = new Date().getTime()
        var time = new Date()
        time.setSeconds(0)
        this.reminder.time = RemindersUtils.translateLocalTimeToTimestamp(time)
        this.reminder.id = Utils.generateUID();

    } else if (this.reminder.frequency !== "days-of-week") {
        var d = new Date()
        d.setFullYear(this.reminder.year)
        d.setDate(this.reminder.dayOfMonth)
        d.setMonth(this.reminder.month - 1)
        d.setHours(0)
        d.setMinutes(0)
        d.setSeconds(0)
        this.reminder.date = d.getTime()
    }

    var itemDialog = this
    document.getElementById("date").onclick = function () {
        itemDialog.dialog.close()
        const picker = new MaterialDatetimePicker({
            default: moment(itemDialog.date),
        }).on('open', function () {
            this.pickerEl.classList.add("reminder-calendar-picker");
            this.pickerEl.classList.add("reminder-date-picker");

            console.log('opened ' + this.scrimEl)
        })
            .on('submit', (val) => {
                itemDialog.setDate(val)
                itemDialog.dialog.showModal()
            })
            .on('cancel', () => {
                itemDialog.dialog.showModal()
            });
        picker.open();
    }
    document.getElementById("reminder-time-picker").onchange = function () {
        var array = document.getElementById("reminder-time-picker").value.split(":")
        itemDialog.time = array[0] * 60 * 60 * 1000 + array[1] * 60 * 1000

    }
    this.okButton = element.getElementsByClassName("ok")[0]
    this.okButton.onclick = function () {
        if (itemDialog.getFrequency() != undefined && itemDialog.getFrequency().length > 0) {
            itemDialog.dialog.close()
            if (itemDialog.reminder != undefined) {
                var i = 0;
                if (writer.note.metadata.reminders == undefined)
                    writer.note.metadata.reminders = []
                for (var reminder of writer.note.metadata.reminders) {
                    if (itemDialog.reminder.id == reminder.id) {
                        writer.note.metadata.reminders.splice(i, 1);
                    }
                    i++;
                }
            }
            itemDialog.reminder.frequency = itemDialog.getFrequency()
            if (itemDialog.reminder.frequency == "days-of-week")
                itemDialog.reminder.days = itemDialog.getDays();
            else {
                itemDialog.reminder.date = itemDialog.date;
                itemDialog.reminder.dayOfMonth = itemDialog.dayOfMonth
                itemDialog.reminder.month = itemDialog.month
                itemDialog.reminder.year = itemDialog.year

            }
            itemDialog.reminder.time = itemDialog.time
            if (writer.note.metadata.reminders == undefined)
                writer.note.metadata.reminders = []
            writer.note.metadata.reminders.push(itemDialog.reminder)
            writer.hasTextChanged = true;
            saveTextIfChanged()
            writer.openRemindersDialog()
        }
    }
    this.frequencyInput = document.getElementById("frequency")
    this.frequencyValueInput = document.getElementById("frequency-val")
    this.onFrequencyChanged();
    this.frequencyContainer = element.getElementsByClassName('frequency-container')[0]
    for (var opt of this.frequencyContainer.getElementsByClassName('mdl-menu__item')) {
        opt.onclick = function (event) {
            itemDialog.setFrequency(event.target.getAttribute("data-val"))
        }
    }
    itemDialog.time = this.reminder.time
    this.setTime(this.reminder.time)
    this.setDate(this.reminder.date)
    this.setFrequency(this.reminder.frequency)
    if (this.reminder.frequency == "days-of-week") {

        for (var day of this.reminder.days) {
            for (var dayInput of document.getElementsByName("days[]")) {
                if (dayInput.value == day)
                    dayInput.checked = true
            }
        }
    }

}
ReminderItemDialog.prototype.getDays = function () {
    var days = []
    for (var day of document.getElementsByName("days[]")) {
        console.log(day.value + " " + day.checked)
        if (day.checked)
            days.push(day.value)
    }
    return days

}
ReminderItemDialog.prototype.setFrequency = function (freq) {
    this.frequencyValueInput.value = freq
    this.frequencyInput.value = $.i18n(freq)
    this.onFrequencyChanged();
}

ReminderItemDialog.prototype.getFrequency = function () {
    return this.frequencyValueInput.value;
}

ReminderItemDialog.prototype.onFrequencyChanged = function () {
    if (this.frequencyValueInput.value == "days-of-week") {
        document.getElementById("days-selector").style.display = "block";
        document.getElementById("date-container").style.display = "none";

    } else {
        document.getElementById("days-selector").style.display = "none";
        document.getElementById("date-container").style.display = "block";

    }
}


ReminderItemDialog.prototype.setDate = function (date) {
    var d = new Date(date);
    this.dayOfMonth = d.getDate()
    this.month = d.getMonth() + 1
    this.year = d.getFullYear()

    this.date = d.getTime();
    document.getElementById("date").value = d.toLocaleDateString();
}

ReminderItemDialog.prototype.setTime = function (time) {
    var date = new Date(time)
    document.getElementById("reminder-time-picker").value = ("0" + date.getUTCHours()).slice(-2) + ":" + ("0" + date.getMinutes()).slice(-2);//cheat to have two digits
}