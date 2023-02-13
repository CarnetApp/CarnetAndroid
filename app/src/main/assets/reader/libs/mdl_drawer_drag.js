function initDragAreas() {

    var selected = null, // Object of the element to be moved
        x_pos = 0, y_pos = 0, // Stores x & y coordinates of the mouse pointer
        x_elem = 0, y_elem = 0, x_init = 0; // Stores top, left values (edge) of the element

    var elem_height = 0;
    var elem_width = 0;
    var original_event = undefined;
    var init_translation = undefined;
    function _drag_init(elem, event) {
        if (document.getElementsByClassName("is-small-screen").length <= 0)
            return;
        selected = elem;
        original_event = event;
        var boundingRectangle = selected.getBoundingClientRect();

        y_elem = (selected.offsetHeight - (boundingRectangle.bottom - boundingRectangle.top)) / 2;
        x_init = event.clientX
        if (x_init == undefined)
            x_init = event.changedTouches[0].clientX
        console.log(event)
        init_translation = parseInt(window.getComputedStyle(selected).transform.split(',')[4], 10);
        selected.style.transitionDuration = "unset";
        document.addEventListener('mousemove', _move_elem, false);
        document.addEventListener('mouseup', _destroy, false);
        document.addEventListener('touchmove', _move_elem, false);
        document.addEventListener('touchend', _destroy, false);
    };

    // Will be called when user dragging an element
    function _move_elem(e) {
        if (e.clientX == undefined)
            e.clientX = e.changedTouches[0].clientX
        x_pos = e.clientX - x_init;
        if (x_pos > 10 || 10 > x_pos) {
            insideDragArea.style.display = "block" //prevent click
        }
        y_pos = e.clientY;
        const next = (x_pos + init_translation);
        console.log("moving" + next)

        if (next <= 0)
            selected.style.transform = "translateX(" + next + 'px' + ")";
    }
    function _destroy(e) {

        selected.style.transitionDuration = "";

        if (init_translation == 0 && parseInt(window.getComputedStyle(selected).transform.split(',')[4], 10) < -50 || init_translation != 0 && parseInt(window.getComputedStyle(selected).transform.split(',')[4], 10) < -220) {
            selected.classList.remove("is-visible");
            document.getElementsByClassName("mdl-layout__obfuscator")[0].classList.remove("is-visible");
        }
        else {
            selected.classList.add("is-visible");
            document.getElementsByClassName("mdl-layout__obfuscator")[0].classList.add("is-visible");

        }
        selected.style.transform = ""
        selected = null;

        document.removeEventListener('mousemove', _move_elem);
        document.removeEventListener('touchmove', _move_elem);

        document.removeEventListener('mouseup', _destroy);
        document.removeEventListener('touchend', _destroy);

        insideDragArea.style.display = "none"
    }

    const drawer = document.getElementsByClassName("mdl-layout__drawer")[0];
    // Bind the functions...
    drawer.onmousedown = function (e) {
        _drag_init(document.getElementsByClassName("mdl-layout__drawer")[0], e);
    };
    drawer.ontouchstart = function (e) {
        _drag_init(document.getElementsByClassName("mdl-layout__drawer")[0], e);
    };
    const dragArea = document.createElement("div");
    dragArea.onmousedown = function (e) {
        _drag_init(document.getElementsByClassName("mdl-layout__drawer")[0], e);
    };
    dragArea.ontouchstart = function (e) {
        _drag_init(document.getElementsByClassName("mdl-layout__drawer")[0], e);
    };

    dragArea.id = "drawer-drag-area";
    dragArea.style.position = "absolute";
    dragArea.style.zIndex = "2";
    dragArea.style.height = "100%";
    dragArea.style.width = "10px";
    dragArea.style.left = "0";
    drawer.parentNode.insertBefore(dragArea, drawer);

    const insideDragArea = document.createElement("div");

    insideDragArea.id = "drawer-inside-drag-area";
    insideDragArea.style.position = "absolute";
    insideDragArea.style.zIndex = "2";
    insideDragArea.style.height = "100%";
    insideDragArea.style.width = "100%";
    insideDragArea.style.left = "0";
    insideDragArea.style.display = "none";
    drawer.appendChild(insideDragArea);
}

