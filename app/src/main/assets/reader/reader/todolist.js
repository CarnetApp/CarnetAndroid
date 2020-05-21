"use strict";

function generateUID() {
  // I generate the UID from two parts here
  // to ensure the random number provide enough bits.
  var firstPart = Math.random() * 46656 | 0;
  var secondPart = Math.random() * 46656 | 0;
  firstPart = ("000" + firstPart.toString(36)).slice(-3);
  secondPart = ("000" + secondPart.toString(36)).slice(-3);
  return firstPart + secondPart;
}

var TodoListManager = function TodoListManager(element) {
  this.element = element;
  this.todolists = [];
};

TodoListManager.prototype.createTodolist = function (data) {
  var todolistDiv = undefined;
  var manager = this;
  var hasFound = false;

  if (data != undefined && data.id !== undefined) {
    todolistDiv = document.getElementById(data.id);
  }

  if (todolistDiv == undefined) todolistDiv = document.createElement("div");else {
    hasFound = true;
    todolistDiv.innerHTML = "";
  }
  if (data != undefined && data.id != undefined) todolistDiv.id = data.id;else todolistDiv.id = "todolist" + generateUID();
  todolistDiv.innerHTML = "";
  todolistDiv.contentEditable = false;
  todolistDiv.classList.add("todo-list");
  var todolistContent = document.createElement("div");
  todolistContent.classList.add("todo-list-content");
  var deleteElem = document.createElement("a");
  deleteElem.classList.add("remove-todo-list");
  deleteElem.classList.add("mdl-button");
  deleteElem.innerHTML = $.i18n("remove_todo_list");
  var todoTitle = document.createElement("h3");
  todoTitle.innerHTML = $.i18n("todo");
  var doneTitle = document.createElement("h3");
  doneTitle.innerHTML = $.i18n("completed");
  var todo = document.createElement("div");
  todo.classList.add("todo");
  todo.id = "todooo" + generateUID();
  var done = document.createElement("div");
  done.classList.add("done");
  todolistContent.appendChild(deleteElem);
  todolistContent.appendChild(todoTitle);
  todolistContent.appendChild(todo);
  todolistDiv.todo = todo;
  var addItem = document.createElement("button");
  addItem.innerHTML = $.i18n("add_item");
  addItem.classList.add("add-todolist-item");
  addItem.classList.add("mdl-button");
  addItem.classList.add("mdl-js-button");
  addItem.classList.add("mdl-js-ripple-effect");
  addItem.href = "#";
  todolistContent.appendChild(addItem);
  todolistContent.appendChild(doneTitle);
  todolistContent.appendChild(done);
  todolistDiv.done = done;
  todolistDiv.appendChild(todolistContent);

  if (!hasFound) {
    this.element.appendChild(todolistDiv);
  }

  var todolist = new TodoList(todolistDiv);
  if (data != undefined) todolist.fromData(data);
  this.todolists.push(todolist);

  deleteElem.onclick = function () {
    console.log("click remove");

    writer.genericDialog.querySelector(".action").onclick = function () {
      manager.removeTodolist(todolistDiv.id);
      writer.genericDialog.close();
    };

    writer.genericDialog.querySelector(".cancel").onclick = function () {
      writer.genericDialog.close();
    };

    writer.genericDialog.querySelector(".action").innerHTML = $.i18n("ok");
    writer.genericDialog.querySelector(".content").innerHTML = $.i18n("delete_todolist_confirmation");
    writer.genericDialog.showModal();
    return true;
  };

  console.log("todoodod");

  var isWindowLarge = function isWindowLarge() {
    return $(writer.oCenter).width() > 920;
  };

  self.setAddItem = function (event) {
    if (event != undefined && event.type == "resize" && isWindowLarge() != window.wasLarge) {
      addItem.isRelative = undefined;
      addItem.isDisplayed = undefined;
    }

    window.wasLarge = isWindowLarge();

    var setFixed = function setFixed() {
      addItem.isRelative = false;
      addItem.style.position = "fixed";
      addItem.style.bottom = "10px";
      addItem.style.marginTop = "unset";
      if (isWindowLarge()) addItem.style.right = "80px";else addItem.style.right = "20px";
    };

    var setRelative = function setRelative() {
      addItem.isRelative = true;
      addItem.style.position = "absolute";
      addItem.style.bottom = "unset";
      addItem.style.top = "unset";
      addItem.style.marginTop = "13px";
      if (isWindowLarge()) addItem.style.right = "68px";else addItem.style.right = "8px";
    };

    var rect = todolistDiv.getBoundingClientRect();
    var rectdoneTitle = doneTitle.getBoundingClientRect();

    if (rect.top + 120 < $(writer.oCenter).height()) {
      if (!addItem.isDisplayed || addItem.isDisplayed == undefined) {
        addItem.isDisplayed = true;
        $(addItem).fadeIn();
      }

      if (rectdoneTitle.bottom < $(writer.oCenter).height() + 50) {
        if (!addItem.isRelative || addItem.isRelative == undefined) setRelative();
      } else if (addItem.isRelative || addItem.isRelative == undefined) {
        setFixed();
      }
    } else if (addItem.isDisplayed || addItem.isDisplayed == undefined) {
      $(addItem).fadeOut();
      addItem.isDisplayed = false;
    }
  };

  writer.oCenter.addEventListener("scroll", setAddItem);
  $(window).on('resize', setAddItem);
  setAddItem();
  return todolist;
};

TodoListManager.prototype.fromData = function (data) {
  for (var i = 0; i < data.length; i++) {
    this.createTodolist(data[i]);
  }
};

TodoListManager.prototype.removeTodolist = function (id) {
  console.log("remove");
  var event = new Event('remove-todolist');
  event.id = id;
  event.previous = document.getElementById(id).previousSibling;
  event.next = document.getElementById(id).nextElementSibling;
  console.log("remove next");
  console.log(document.getElementById(id).nextElementSibling);
  writer.oCenter.removeEventListener("scroll", setAddItem);
  $(window).off('resize', setAddItem);
  this.element.dispatchEvent(event);
  $("#" + id).remove();

  for (var i = 0; i < this.todolists.length; i++) {
    var todol = this.todolists[i];

    if (todol.element.id === id) {
      this.todolists.splice(i, 1);
      break;
    }
  }
};

TodoListManager.prototype.toData = function () {
  var data = [];

  for (var i = 0; i < this.todolists.length; i++) {
    data.push(this.todolists[i].toData());
  }

  return data;
};

var TodoList = function TodoList(element) {
  var todolist = this;
  this.element = element;
  this.stats = [];
  this.todo = element.todo;
  this.done = element.done;
  this.addItem = element.getElementsByClassName("add-todolist-item")[0];
  if (this.addItem != undefined) this.addItem.onclick = function () {
    todolist.createItem("", false, undefined, true);
  };
  $(this.todo).sortable({
    handle: ".move-item",
    stop: function stop() {
      writer.hasTextChanged = true;
    }
  });
  $(this.todo).disableSelection();
};

TodoList.prototype.toData = function () {
  var result = {};
  if (this.todo == undefined) return result;
  var todo = [];
  var done = []; //separated ids to keep compatibility

  var todoIds = [];
  var doneIds = [];
  var todoChildren = this.todo.childNodes;

  for (var i = 0; i < todoChildren.length; i++) {
    if (todoChildren[i].span != undefined) {
      todo.push(todoChildren[i].span.value);
      todoIds.push(todoChildren[i].itemId);
    }
  }

  var todoChildren = this.done.childNodes;

  for (var i = 0; i < todoChildren.length; i++) {
    if (todoChildren[i].span != undefined) {
      done.push(todoChildren[i].span.value);
      doneIds.push(todoChildren[i].itemId);
    }
  }

  result.id = this.element.id;
  result.todo = todo;
  result.done = done;
  result.stats = this.stats;
  result.todoIds = todoIds;
  result.doneIds = doneIds;
  return result;
};

TodoList.prototype.fromData = function (data) {
  if (data.todo == undefined) return;

  for (var i = 0; i < data.todo.length; i++) {
    this.createItem(data.todo[i], false, undefined, false, data.todoIds != undefined ? data.todoIds[i] : undefined);
  }

  for (var i = 0; i < data.done.length; i++) {
    this.createItem(data.done[i], true, undefined, false, data.doneIds != undefined ? data.doneIds[i] : undefined);
  }

  if (data.stats != undefined) this.stats = data.stats;
};

TodoList.prototype.removeItem = function (item) {
  var todolist = this;
  $(item.span).off('focus', item.span.resizeListener);
  $(item).animate({
    height: '0px'
  }, 150, function () {
    if (item.previousSibling != undefined && item.previousSibling.span != undefined) item.previousSibling.span.focus();
    $(item).remove();
    var event = new Event('todolist-changed');
    todolist.element.parentNode.dispatchEvent(event);
  });
};

TodoList.prototype.createItem = function (text, ischecked, after, scroll, itemId) {
  var todolist = this;
  var id = generateUID();
  if (itemId == undefined) itemId = id;
  var div = document.createElement("div");
  div.classList.add("todo-item");
  div.id = "item" + id;
  var move = document.createElement("span");
  move.innerHTML = "|||";
  move.classList.add("move-item");
  move.classList.add("block-scroll");
  move.addEventListener('mousedown', function () {
    move.classList.add("grabbing");
  }, false);
  move.addEventListener('mouseup', function () {
    move.classList.remove("grabbing");
  }, false);
  div.appendChild(move);
  var label = document.createElement("label");
  label.classList.add("mdl-checkbox");
  label.classList.add("mdl-js-checkbox");
  label.classList.add("mdl-js-ripple-effect");
  label["for"] = id;
  var input = document.createElement("input");
  input.type = "checkbox";
  input.id = id;
  input.classList.add("mdl-checkbox__input");
  label.appendChild(input);
  var span = document.createElement("textarea");
  span.dir = "auto";
  span.rows = 1;
  span.classList.add("mdl-checkbox__label");
  span.classList.add("todo-item-text");
  span.contentEditable = true;
  span.div = div;
  $(span).keydown(function (e) {
    var key = e.which || e.keyCode;

    if (e.ctrlKey && key === 13) {
      if (span.value.trim().length > 0) {
        // 13 is enter
        todolist.createItem("", false, div, true);
      } else {
        e.preventDefault();
      }
    }

    if (key === 8 && span.value.length == 0) {
      todolist.removeItem(div);
    }

    setTimeout(function () {
      resizeTextArea(span);
    }, 20);
  });
  span.value = text;

  span.onclick = function () {
    span.focus();
    return false;
  };

  label.appendChild(span);
  div.label = label;
  div.span = span;
  div.itemId = itemId;
  div.appendChild(label);
  input.oldonchange = input.onchange;

  input.onchange = function () {
    var oldHeight = $(div).height();
    setTimeout(function () {
      $(div).animate({
        height: '0px'
      }, 150, function () {
        $(div).remove();

        if (label.classList.contains("is-checked")) {
          todolist.check(div);
        } else {
          todolist.uncheck(div);
        }

        $(div).animate({
          height: oldHeight
        }, 150, function () {
          div.style.height = "auto";
        });
      });
    }, 100);
  };

  var remove = document.createElement("span");
  remove.innerHTML = "X";
  remove.classList.add("remove-item");
  remove.classList.add("mdl-button");

  remove.onclick = function () {
    todolist.removeItem(div);
  };

  div.appendChild(remove);
  div.material = new window['MaterialCheckbox'](label);
  if (ischecked) this.check(div);else this.uncheck(div, after);

  span.resizeListener = function () {
    if ($(span).is(':focus')) {
      var bottom = div.getBoundingClientRect().bottom + 56;
      if (bottom > $(writer.oCenter).height()) $(writer.oCenter).animate({
        scrollTop: $(writer.oCenter).scrollTop() + bottom - $(writer.oCenter).height()
      }, 'fast');
    }
  }; // $(window).on('resize', span.resizeListener);


  $(span).on('focus', span.resizeListener);

  if (scroll) {
    span.focus();
  }

  resizeTextArea(span);
  return div;
};

TodoList.prototype.check = function (item) {
  if (item.parentNode != null) item.parentNode.removeChild(item);
  item.label.classList.add("is-checked");
  item.material.check();
  if (this.done != undefined) this.done.appendChild(item);
  this.stats.push({
    action: "check",
    time: Date.now(),
    itemId: item.itemId
  });
  var event = new Event('todolist-changed');
  this.element.parentNode.dispatchEvent(event);
};

TodoList.prototype.uncheck = function (item, after) {
  if (item.parentNode != null) item.parentNode.removeChild(item);
  if (after != undefined) this.todo.insertBefore(item, after.nextSibling);else {
    this.todo.appendChild(item);
  }
  item.material.uncheck();
  this.stats.push({
    action: "uncheck",
    time: Date.now(),
    itemId: item.itemId
  });
  var event = new Event('todolist-changed');
  this.element.parentNode.dispatchEvent(event);
};

function resizeTextArea(textarea) {
  textarea.style.height = ""; //hack

  if (textarea.scrollHeight == 46) textarea.style.height = 26 + "px";else textarea.style.height = textarea.scrollHeight + "px";
}