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
  deleteElem.innerHTML = "x";
  var todoTitle = document.createElement("h3");
  todoTitle.innerHTML = "To do";
  var doneTitle = document.createElement("h3");
  doneTitle.innerHTML = "Completed";
  var todo = document.createElement("div");
  todo.classList.add("todo");
  todo.id = "todooo" + generateUID();
  var addItem = document.createElement("a");
  addItem.innerHTML = "Add item";
  addItem.classList.add("add-item");
  addItem.href = "#";
  var done = document.createElement("div");
  done.classList.add("done");
  todolistContent.appendChild(deleteElem);
  todolistContent.appendChild(todoTitle);
  todolistContent.appendChild(todo);
  todolistDiv.todo = todo;
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
    manager.removeTodolist(todolistDiv.id);
    return false;
  };

  console.log("todoodod");
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
  this.todo = element.todo;
  this.done = element.done;
  this.addItem = element.getElementsByClassName("add-item")[0];
  if (this.addItem != undefined) this.addItem.onclick = function () {
    todolist.createItem("", false);
  };
  $(this.todo).sortable({
    handle: ".move-item"
  });
  $(this.todo).disableSelection();
};

TodoList.prototype.startMoving = function (item) {
  $(item).draggable();
};

TodoList.prototype.registerToMove = function (hand, item) {
  var todolist = this;

  hand.onclick = function () {
    todolist.startMoving(item);
  };
};

TodoList.prototype.toData = function () {
  var result = {};
  if (this.todo == undefined) return result;
  var todo = [];
  var done = [];
  var todoChildren = this.todo.childNodes;

  for (var i = 0; i < todoChildren.length; i++) {
    if (todoChildren[i].span != undefined) todo.push(todoChildren[i].span.value);
  }

  var todoChildren = this.done.childNodes;

  for (var i = 0; i < todoChildren.length; i++) {
    if (todoChildren[i].span != undefined) done.push(todoChildren[i].span.value);
  }

  result.id = this.element.id;
  result.todo = todo;
  result.done = done;
  return result;
};

TodoList.prototype.fromData = function (data) {
  if (data.todo == undefined) return;

  for (var i = 0; i < data.todo.length; i++) {
    this.createItem(data.todo[i], false);
  }

  for (var i = 0; i < data.done.length; i++) {
    this.createItem(data.done[i], true);
  }
};

TodoList.prototype.removeItem = function (item) {
  var todolist = this;
  $(item).animate({
    height: '0px'
  }, 150, function () {
    if (item.previousSibling != undefined && item.previousSibling.span != undefined) item.previousSibling.span.focus();
    $(item).remove();
    var event = new Event('todolist-changed');
    todolist.element.parentNode.dispatchEvent(event);
  });
};

TodoList.prototype.createItem = function (text, ischecked, after) {
  var todolist = this;
  var id = generateUID();
  var div = document.createElement("div");
  div.classList.add("todo-item");
  div.id = "item" + id;
  var move = document.createElement("span");
  move.innerHTML = "|||";
  move.classList.add("move-item");
  move.addEventListener('mousedown', function () {
    move.classList.add("grabbing");
  }, false);
  move.addEventListener('mouseup', function () {
    move.classList.remove("grabbing");
  }, false); //this.registerToMove(move, div)

  div.appendChild(move);
  var label = document.createElement("label");
  label.classList.add("mdl-checkbox");
  label.classList.add("mdl-js-checkbox");
  label.classList.add("mdl-js-ripple-effect");
  label.for = id;
  var input = document.createElement("input");
  input.type = "checkbox";
  input.id = id;
  input.classList.add("mdl-checkbox__input");
  label.appendChild(input);
  var span = document.createElement("input");
  span.classList.add("mdl-checkbox__label");
  span.classList.add("todo-item-text");
  span.contentEditable = true;
  span.div = div;
  $(span).keydown(function (e) {
    var key = e.which || e.keyCode;
    console.log("key " + key + " lenght " + span.value.trim().length);

    if (key === 13) {
      if (span.value.trim().length > 0) {
        // 13 is enter
        todolist.createItem("", false, div).span.focus();
      } else {
        e.preventDefault();
      }
    }

    console.log("removing? " + span.value.length);

    if (key === 8 && span.value.trim().length == 0) {
      console.log("removing");
      todolist.removeItem(div);
    }
  });
  span.value = text;

  span.onclick = function () {
    span.focus();
    return false;
  };

  label.appendChild(span);
  div.label = label;
  div.span = span;
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

  remove.onclick = function () {
    todolist.removeItem(div);
  };

  div.appendChild(remove);
  div.material = new window['MaterialCheckbox'](label);
  if (ischecked) this.check(div);else this.uncheck(div, after);

  span.onfocus = function () {};

  span.focus();
  return div;
};

TodoList.prototype.check = function (item) {
  if (item.parentNode != null) item.parentNode.removeChild(item);
  item.label.classList.add("is-checked");
  item.material.check();
  if (this.done != undefined) this.done.appendChild(item);
  var event = new Event('todolist-changed');
  this.element.parentNode.dispatchEvent(event);
};

TodoList.prototype.uncheck = function (item, after) {
  if (item.parentNode != null) item.parentNode.removeChild(item);
  if (after != undefined) this.todo.insertBefore(item, after.nextSibling);else {
    this.todo.appendChild(item);
  }
  item.material.uncheck();
  var event = new Event('todolist-changed');
  this.element.parentNode.dispatchEvent(event);
};