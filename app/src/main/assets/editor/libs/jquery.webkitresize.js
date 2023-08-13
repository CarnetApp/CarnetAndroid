/* 
* Webkitresize (http://editorboost.net/webkitresize)
* Copyright 2012 Editorboost. All rights reserved. 
*
* Webkitresize commercial licenses may be obtained at http://editorboost.net/home/licenses.
* If you do not own a commercial license, this file shall be governed by the
* GNU General Public License (GPL) version 3. For GPL requirements, please
* review: http://www.gnu.org/copyleft/gpl.html
*
* Version date: March 19 2013
* REQUIRES: jquery 1.7.1+
*/

; (function ($) {
    $.fn.webkitimageresize = function (options) {
        return this.each(function () {

           

			
            var settings = $.extend({
            }, options);

            var lastCrc;
            var imageResizeinProgress = false;
            var imageMoveinProgress = false;
            var currentImage;
            var currentImage_HxW_Rate;
            var currentImage_WxH_Rate;
            var initialHeight;
            var initialWidth;

            var methods = {

                removeResizeElements: function (context) {
                    $(".img-resize-selector").remove();
                    $(".img-resize-region").remove();
                },

                drawResizeElements: function(context, img, imgHeight, imgWidth, imgPosition){
                    context.$docBody.append("<img onclick=\"app.editImage('"+img.src+"');\" class='img-resize-region img-edit' src=\"images/image.png\"  style='position:absolute;top:" + imgPosition.top + "px;left:" + imgPosition.left + "px;' />");
                    context.$docBody.append("<span class='img-resize-selector' style='margin:10px;position:absolute;top:" + (imgPosition.top + imgHeight - 10) + "px;left:" + (imgPosition.left + imgWidth - 10) + "px;border:solid 2px red;width:20px;height:20px;cursor:se-resize;z-index:1;background-color:red;opacity: 0.60;-ms-filter: \"progid:DXImageTransform.Microsoft.Alpha(Opacity=60)\";filter: alpha(opacity=60);-moz-opacity: 0.6;''></span>");
                    context.$docBody.append("<span class='img-resize-region region-top-right' style='position:absolute;top:" + imgPosition.top + "px;left:" + imgPosition.left + "px;border:dashed 1px grey;width:" + imgWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span class='img-resize-region region-top-down' style='position:absolute;top:" + imgPosition.top + "px;left:" + imgPosition.left + "px;border:dashed 1px grey;width:0px;height:" + imgHeight + "px;'></span>");

                    context.$docBody.append("<span class='img-resize-region region-right-down' style='position:absolute;top:" + imgPosition.top + "px;left:" + (imgPosition.left + imgWidth) + "px;border:dashed 1px grey;width:0px;height:" + imgHeight + "px;'></span>");
                    context.$docBody.append("<span class='img-resize-region region-down-left' style='position:absolute;top:" + (imgPosition.top + imgHeight) + "px;left:" + imgPosition.left + "px;border:dashed 1px grey;width:" + imgWidth + "px;height:0px;'></span>");
                },

                imageClick: function (context, img) {
                    if (settings.beforeElementSelect) {
                        settings.beforeElementSelect(img);
                    }

                    methods.removeResizeElements();
                    currentImage = img;

                    var imgHeight = $(img).outerHeight();
                    var imgWidth = $(img).outerWidth();
                    var imgPosition = $(img).offset();

                    methods.drawResizeElements(context, img, imgHeight, imgWidth, imgPosition);

                    var dragStop = function () {                    
                        if (imageResizeinProgress) {

                                 $(currentImage).css("width", $(".region-top-right").width() + "px")
                                .css('height', $(".region-top-down").height() + "px");
                            methods.refresh(context);
                            $(currentImage).click();

                            $container.trigger('webkitresize-updatecrc', [methods.crc(context.$container.html())]);

                            imageResizeinProgress = false;

                            if (settings.afterResize) {
                                settings.afterResize(currentImage);
                            }
                        }
                    };

                    var windowMouseMove = function (e) {
                        if (imageResizeinProgress) {

                            var resWidth = imgWidth;
                            var resHeight = imgHeight;

                            resHeight = e.pageY - imgPosition.top;
                            resWidth = e.pageX - imgPosition.left;

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }
                            
                            if(e.ctrlKey){
                                var heightDiff = initialHeight - resHeight;
                                if(heightDiff < 0){
                                    heightDiff = heightDiff * -1.0;
                                }
                                var widthDiff = initialWidth - resWidth;
                                if(widthDiff < 0){
                                    widthDiff = widthDiff * -1.0;
                                }

                                if(heightDiff > widthDiff){
                                    resWidth = resHeight * currentImage_WxH_Rate;
                                }
                                else{
                                    resHeight = resWidth * currentImage_HxW_Rate;
                                }       
                            }                        

                            $(".img-resize-selector").css("top", (imgPosition.top + resHeight - 10) + 'px').css("left", (imgPosition.left + resWidth - 10) + "px");
                            $(".region-top-right").css("width", resWidth + "px");
                            $(".region-top-down").css("height", resHeight + "px");

                            $(".region-right-down").css("left", (imgPosition.left + resWidth) + "px").css("height", resHeight + "px");
                            $(".region-down-left").css("top", (imgPosition.top + resHeight) + "px").css("width", resWidth + "px");
                        }
                        else if(imageMoveinProgress){
                            //alert("moving");
                        }

                        return false;
                    };

                     $(currentImage).bind('vmousedown', function(e){
                        imageMoveinProgress = true;
                     });

                $(".img-edit").bind('vmouseup', function(e){
                        app.editImage(currentImage.src, currentImage.id);

                        return true;
                    });

                $(".img-resize-selector").bind('vmousedown', function(e){
              
                        if (settings.beforeResizeStart) {
                            settings.beforeResizeStart(currentImage);
                        }
                        
                        var imgH = $(currentImage).height();
                        var imgW = $(currentImage).width();

                        currentImage_HxW_Rate = imgH / imgW;
                        currentImage_WxH_Rate = imgW / imgH;
                        if(imgH > imgW){
                            initialHeight = 0;
                            initialWidth = (imgH - imgW) * -1;
                        }
                        else{
                            initialWidth = 0;
                            initialHeight = (imgW - imgH) * -1;
                        }

                        imageResizeinProgress = true;

                        return false;
                    });

                      $(window.document).bind('vmouseup', function(e){
                        imageMoveinProgress = false;
                        if (imageResizeinProgress) {
                            dragStop();
                        }

                    });
					  $(window.document).bind('vmousemove', function(e){
						 windowMouseMove(e);
					});


                    if (settings.afterElementSelect) {
                        settings.afterElementSelect(currentImage);
                    }
                },

                rebind: function (context) {
                    context.$container.find("img").each(function (i, v) {
                        $(v).unbind('click');
                        $(v).draggable();
                        $(v).click(function (e) {
                            if (e.target == v) {
                                methods.imageClick(context, v);
                            }
                        });
                    });
                     context.$container.find("div").each(function (i, v) {
                                            if($(v).hasClass('quicknote-txtzone')){
                                                   $(v).unbind('click');
                                                $(v).draggable({}).click(function() {  }).blur(function() { $(this).draggable({disabled: false}); });;
                                                $(v).click(function (e) {
                                                    if (e.target == v) {
                                                        methods.imageClick(context, v);
                                                        $(v).draggable({disabled: true});
                                                    }
                                                });
                                            }


                                        });
                },

                refresh: function (context) {                                                   
                    methods.rebind(context);
                    
                    methods.removeResizeElements();
                    
                    if (!currentImage) {
                        if (settings.afterRefresh) {
                            settings.afterRefresh(null);
                        }
                        return;
                    }
                    
                    var img = currentImage;

                    var imgHeight = $(img).outerHeight();
                    var imgWidth = $(img).outerWidth();                    
                    var imgPosition = $(img).offset();

                    methods.drawResizeElements(context, img, imgHeight, imgWidth, imgPosition);

                    lastCrc = methods.crc(context.$container.html());

                    if (settings.afterRefresh) {
                        settings.afterRefresh(currentImage);
                    }
                },

                reset: function (context) {
                    if (currentImage != null) {
                        currentImage = null;
                        imageResizeinProgress = false;                        
                        methods.removeResizeElements();

                        if (settings.afterReset) {
                            settings.afterReset();
                        }
                    }
                    methods.rebind(context);
                },

                crc: function (str) {
                    var hash = 0;
                    if (str == null || str.length == 0) return hash;
                    for (i = 0; i < str.length; i++) {
                        char = str.charCodeAt(i);
                        hash = ((hash << 5) - hash) + char;
                        hash = hash & hash;
                    }
                    return hash;
                }
            };

            var container = this;
            var $container = $(this);
            var $docBody = $("body");            

            lastCrc = methods.crc($container.html());

            var context = {
                container: container,
                $container: $container,
                $docBody: $docBody
            };

            container.addEventListener('scroll', function () {
                methods.reset(context);
            }, false);

            $(document).mouseup(function (e) {
                if(!imageResizeinProgress){
                    if (lastCrc != methods.crc($container.html())) {
                        methods.reset(context);
                    }
                    else {
                        var x = (e.x) ? e.x : e.clientX;
                        var y = (e.y) ? e.y : e.clientY;
                        var mouseUpElement = document.elementFromPoint(x, y);
                        if (mouseUpElement) {
                            if (!$(mouseUpElement).is("img")) {
                                methods.reset(context);
                            }
                        }
                        else {
                            methods.reset(context);
                        }
                    }
                }
            });            

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });


            if (!container.crcChecker) {
                container.crcChecker = setInterval(function () {
                    var currentCrc = methods.crc($container.html());
                    if (lastCrc != currentCrc) {
                        $container.trigger('webkitresize-crcchanged', [currentCrc]);
                    }
                }, 1000);
            }

            $(window).resize(function(){
                methods.reset(context);
            });

            $container.bind('webkitresize-crcchanged', function (event, crc) {
                lastCrc = crc;
                methods.reset(context);
            });

            $container.bind('webkitresize-updatecrc', function (event, crc) {
                lastCrc = crc;
            });

            methods.refresh(context);

        });
    };


    $.fn.webkittableresize = function (options) {
        return this.each(function () {

            if (!$.browser.webkit) {
                return;
            }


            var settings = $.extend({
            }, options);

            var lastCrc;
            var tableResizeinProgress = false;
            var currenttable;

            var methods = {

                removeResizeElements: function (context) {
                    $(".resize-selector").remove();
                    $(".resize-region").remove();
                },

                drawResizeElements: function(context, tbl, tblHeight, tblWidth, tblPosition){
                    context.$docBody.append("<span class='resize-selector' style='margin:10px;position:absolute;top:" + (tblPosition.top + tblHeight - 10) + "px;left:" + (tblPosition.left + tblWidth - 10) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1;background-color:red;opacity: 0.60;-ms-filter: \"progid:DXImageTransform.Microsoft.Alpha(Opacity=60)\";filter: alpha(opacity=60);-moz-opacity: 0.6;''></span>");

                    context.$docBody.append("<span class='resize-region region-top-right' style='position:absolute;top:" + (tblPosition.top) + "px;left:" + (tblPosition.left) + "px;border:dashed 1px grey;width:" + tblWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span class='resize-region region-top-down' style='position:absolute;top:" + (tblPosition.top) + "px;left:" + (tblPosition.left) + "px;border:dashed 1px grey;width:0px;height:" + tblHeight + "px;'></span>");

                    context.$docBody.append("<span class='resize-region region-right-down' style='position:absolute;top:" + (tblPosition.top) + "px;left:" + (tblPosition.left + tblWidth) + "px;border:dashed 1px grey;width:0px;height:" + tblHeight + "px;'></span>");
                    context.$docBody.append("<span class='resize-region region-down-left' style='position:absolute;top:" + (tblPosition.top + tblHeight) + "px;left:" + (tblPosition.left) + "px;border:dashed 1px grey;width:" + tblWidth + "px;height:0px;'></span>");
                },

                tableClick: function (context, tbl) {
                    if (settings.beforeElementSelect) {
                        settings.beforeElementSelect(tbl);
                    }

                    methods.removeResizeElements();
                    currenttable = tbl;

                    var tblHeight = $(tbl).outerHeight();
                    var tblWidth = $(tbl).outerWidth();
                    var tblPosition = $(tbl).offset();


                    methods.drawResizeElements(context, tbl, tblHeight, tblWidth, tblPosition); 

                    var dragStop = function () {
                        if (tableResizeinProgress) {
                            $(currenttable)
                                .css("width", $(".region-top-right").width() + "px")
                                .css('height', $(".region-top-down").height() + "px");
                            methods.refresh(context);
                            var ce = currenttable;

                            $container.trigger('webkitresize-updatecrc', [methods.crc(context.$container.html())]);
                            $container.trigger('webkitresize-table-resized');

                            tableResizeinProgress = false;
                            setTimeout(function(){
                                methods.reset(context);
                                methods.tableClick(context, ce);
                            }, 300);

                            if (settings.afterResize) {
                                settings.afterResize(currenttable);
                            }
                        }
                    };

                    var windowMouseMove = function (e) {
                        if (tableResizeinProgress) {

                            var resWidth = tblWidth;
                            var resHeight = tblHeight;

                            resHeight = e.pageY - (tblPosition.top);
                            resWidth = e.pageX - (tblPosition.left);

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }

                            $(".resize-selector").css("top", (tblPosition.top + resHeight - 10) + 'px').css("left", (tblPosition.left + resWidth - 10) + "px");
                            $(".region-top-right").css("width", resWidth + "px");
                            $(".region-top-down").css("height", resHeight + "px");

                            $(".region-right-down").css("left", (tblPosition.left + resWidth) + "px").css("height", resHeight + "px");
                            $(".region-down-left").css("top", (tblPosition.top + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };

                    $(".resize-selector").mousedown(function (e) {
                        if (settings.beforeResizeStart) {
                            settings.beforeResizeStart(currenttable);
                        }
                        tableResizeinProgress = true;
                        return false;
                    });

                    $("*").mouseup(function () {
                        if (tableResizeinProgress) {
                            dragStop();
                        }
                    });

                    $(window).mousemove(function (e) {
                        windowMouseMove(e);
                    });

                    if (settings.afterElementSelect) {
                        settings.afterElementSelect(currenttable);
                    }
                },

                rebind: function (context) {
                    context.$container.find("table").each(function (i, v) {
                        $(v).unbind('click');
                        $(v).click(function (e) {
                            if (e.target == v || ($(e.target).is('td') && $(e.target).parents("table")[0] == v)) {
                                methods.tableClick(context, v);
                            }
                        });
                    });
                },

                refresh: function (context) {
                    methods.rebind(context);

                    methods.removeResizeElements();

                    if (!currenttable) {
                        if (settings.afterRefresh) {
                            settings.afterRefresh(null);
                        }
                        return;
                    }

                    var tbl = currenttable;

                    var tblHeight = $(tbl).outerHeight();
                    var tblWidth = $(tbl).outerWidth();
                    var tblPosition = $(tbl).offset();

                    methods.drawResizeElements(context, tbl, tblHeight, tblWidth, tblPosition); 

                    lastCrc = methods.crc(context.$container.html());

                    if (settings.afterRefresh) {
                        settings.afterRefresh(currenttable);
                    }
                },

                reset: function (context) {
                    if (currenttable != null) {
                        currenttable = null;
                        tableResizeinProgress = false;
                        methods.removeResizeElements();

                        if (settings.afterReset) {
                            settings.afterReset();
                        }
                    }

                    methods.rebind(context);
                },

                crc: function (str) {
                    var hash = 0;
                    if (str == null || str.length == 0) return hash;
                    for (i = 0; i < str.length; i++) {
                        char = str.charCodeAt(i);
                        hash = ((hash << 5) - hash) + char;
                        hash = hash & hash;
                    }
                    return hash;
                }

            };

            var container = this;
            var $container = $(this);
            var $docBody = $("body");

            lastCrc = methods.crc($container.html());

            var context = {
                container: container,
                $container: $container,
                $docBody: $docBody
            };

            container.addEventListener('scroll', function () {
                methods.reset(context);
            }, false);

            $(document).mouseup(function (e) {
                if(!tableResizeinProgress){
                    if (lastCrc != methods.crc($container.html())) {
                        methods.reset(context);
                    }
                    else {
                        var x = (e.x) ? e.x : e.clientX;
                        var y = (e.y) ? e.y : e.clientY;
                        var mouseUpElement = document.elementFromPoint(x, y);
                        if (mouseUpElement) {
                            if (!$(mouseUpElement).is("table")) {
                                methods.reset(context);
                            }
                        }
                        else {
                            methods.reset(context);
                        }
                    }
                }
            });

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });

            if (!container.crcChecker) {
                container.crcChecker = setInterval(function () {
                    var currentCrc = methods.crc($container.html());
                    if (lastCrc != currentCrc) {
                        $container.trigger('webkitresize-crcchanged', [currentCrc]);
                    }
                }, 1000);
            }

            $(window).resize(function(){
                methods.reset(context);
            });

            $container.bind('webkitresize-crcchanged', function (event, crc) {
                lastCrc = crc;
                methods.reset(context);
            });

            $container.bind('webkitresize-updatecrc', function (event, crc) {
                lastCrc = crc;
            });

            methods.refresh(context);

        });
    };


    $.fn.webkittdresize = function (options) {
        return this.each(function () {

            if (!$.browser.webkit) {
                return;
            }


            var settings = $.extend({
            }, options);

            var lastCrc;
            var tdResizeinProgress = false;
            var currenttd;

            var methods = {

                removeResizeElements: function (context) {
                    $(".td-resize-selector").remove();
                    $(".td-resize-region").remove();
                },

                drawResizeElements: function(context, td, tdHeight, tdWidth, tdPosition){
                    context.$docBody.append("<span class='td-resize-selector' style='margin:10px;position:absolute;top:" + (tdPosition.top + tdHeight - 10) + "px;left:" + (tdPosition.left + tdWidth - 10) + "px;border:solid 2px red;width:6px;height:6px;cursor:se-resize;z-index:1;background-color:red;opacity: 0.60;-ms-filter: \"progid:DXImageTransform.Microsoft.Alpha(Opacity=60)\";filter: alpha(opacity=60);-moz-opacity: 0.6;''></span>");

                    context.$docBody.append("<span class='td-resize-region td-region-top-right' style='position:absolute;top:" + (tdPosition.top) + "px;left:" + (tdPosition.left) + "px;border:dashed 1px green;width:" + tdWidth + "px;height:0px;'></span>");
                    context.$docBody.append("<span class='td-resize-region td-region-top-down' style='position:absolute;top:" + (tdPosition.top) + "px;left:" + (tdPosition.left) + "px;border:dashed 1px green;width:0px;height:" + tdHeight + "px;'></span>");

                    context.$docBody.append("<span class='td-resize-region td-region-right-down' style='position:absolute;top:" + (tdPosition.top) + "px;left:" + (tdPosition.left + tdWidth) + "px;border:dashed 1px green;width:0px;height:" + tdHeight + "px;'></span>");
                    context.$docBody.append("<span class='td-resize-region td-region-down-left' style='position:absolute;top:" + (tdPosition.top + tdHeight) + "px;left:" + (tdPosition.left) + "px;border:dashed 1px green;width:" + tdWidth + "px;height:0px;'></span>");
                },

                tdClick: function (context, td) {
                    if (settings.beforeElementSelect) {
                        settings.beforeElementSelect(td);
                    }

                    methods.removeResizeElements();
                    currenttd = td;

                    var tdHeight = $(td).outerHeight();
                    var tdWidth = $(td).outerWidth();
                    var tdPosition = $(td).offset();

                    methods.drawResizeElements(context, td, tdHeight, tdWidth, tdPosition);

                    var dragStop = function () {
                        if (tdResizeinProgress) {
                            $(currenttd)
                                .css("width", $(".td-region-top-right").width() + "px")
                                .css('height', $(".td-region-top-down").height() + "px");
                            methods.refresh(context);
                            $(currenttd).click();

                            $container.trigger('webkitresize-updatecrc', [methods.crc(context.$container.html())]);

                            tdResizeinProgress = false;

                            if (settings.afterResize) {
                                settings.afterResize(currenttd);
                            }
                        }
                    };

                    var windowMouseMove = function (e) {
                        if (tdResizeinProgress) {

                            var resWidth = tdWidth;
                            var resHeight = tdHeight;

                            resHeight = e.pageY - (tdPosition.top);
                            resWidth = e.pageX - (tdPosition.left);

                            if (resHeight < 1) {
                                resHeight = 1;
                            }
                            if (resWidth < 1) {
                                resWidth = 1;
                            }

                            $(".td-resize-selector").css("top", (tdPosition.top + resHeight - 10) + 'px').css("left", (tdPosition.left + resWidth - 10) + "px");
                            $(".td-region-top-right").css("width", resWidth + "px");
                            $(".td-region-top-down").css("height", resHeight + "px");

                            $(".td-region-right-down").css("left", (tdPosition.left + resWidth) + "px").css("height", resHeight + "px");
                            $(".td-region-down-left").css("top", (tdPosition.top + resHeight) + "px").css("width", resWidth + "px");
                        }

                        return false;
                    };

                    $(".td-resize-selector").mousedown(function (e) {
                        if (settings.beforeResizeStart) {
                            settings.beforeResizeStart(currenttd);
                        }
                        tdResizeinProgress = true;
                        return false;
                    });

                    $("*").mouseup(function () {
                        if (tdResizeinProgress) {
                            dragStop();
                        }
                    });

                    $(window).mousemove(function (e) {
                        windowMouseMove(e);
                    });

                    if (settings.afterElementSelect) {
                        settings.afterElementSelect(currenttd);
                    }
                },

                rebind: function (context) {
                    context.$container.find("td").each(function (i, v) {
                        $(v).unbind('click');
                        $(v).click(function (e) {
                            if (e.target == v) {
                                methods.tdClick(context, v);
                            }
                        });
                    });
                },

                refresh: function (context) {
                    methods.rebind(context);

                    methods.removeResizeElements();

                    if (!currenttd) {
                        if (settings.afterRefresh) {
                            settings.afterRefresh(null);
                        }
                        return;
                    }

                    var td = currenttd;

                    var tdHeight = $(td).outerHeight();
                    var tdWidth = $(td).outerWidth();
                    var tdPosition = $(td).offset();

                    methods.drawResizeElements(context, td, tdHeight, tdWidth, tdPosition);

                    lastCrc = methods.crc(context.$container.html());

                    if (settings.afterRefresh) {
                        settings.afterRefresh(currenttd);
                    }
                },

                reset: function (context) {
                    if (currenttd != null) {
                        currenttd = null;
                        tdResizeinProgress = false;
                        methods.removeResizeElements();

                        if (settings.afterReset) {
                            settings.afterReset();
                        }
                    }

                    methods.rebind(context);
                },

                crc: function (str) {
                    var hash = 0;
                    if (str == null || str.length == 0) return hash;
                    for (i = 0; i < str.length; i++) {
                        char = str.charCodeAt(i);
                        hash = ((hash << 5) - hash) + char;
                        hash = hash & hash;
                    }
                    return hash;
                }

            };

            var container = this;
            var $container = $(this);
            var $docBody = $("body");            

            lastCrc = methods.crc($container.html());

            var context = {
                container: container,
                $container: $container,
                $docBody: $docBody
            };

            container.addEventListener('scroll', function () {
                methods.reset(context);
            }, false);

            $(document).mouseup(function (e) {
                if (!tdResizeinProgress) {
                    if (lastCrc != methods.crc($container.html())) {
                        methods.reset(context);
                    }
                    else {
                        var x = (e.x) ? e.x : e.clientX;
                        var y = (e.y) ? e.y : e.clientY;
                        var mouseUpElement = document.elementFromPoint(x, y);
                        if (mouseUpElement) {
                            if (!$(mouseUpElement).is("td")) {
                                methods.reset(context);
                            }
                        }
                        else {
                            methods.reset(context);
                        }
                    }
                }
            });

            $(document).keyup(function (e) {
                if (e.keyCode == 27) {
                    methods.reset(context);
                }
            });

            if (!container.crcChecker) {
                container.crcChecker = setInterval(function () {
                    var currentCrc = methods.crc($container.html());
                    if (lastCrc != currentCrc) {
                        $container.trigger('webkitresize-crcchanged', [currentCrc]);
                    }
                }, 1000);
            }

            $(window).resize(function(){
                methods.reset(context);
            });

            $container.bind('webkitresize-crcchanged', function (event, crc) {
                lastCrc = crc;
                methods.reset(context);
            });

            $container.bind('webkitresize-updatecrc', function (event, crc) {
                lastCrc = crc;
            });

            $container.bind('webkitresize-table-resized', function () {
                methods.reset(context);
            });

            methods.refresh(context);

        });
    };
})(jQuery);
