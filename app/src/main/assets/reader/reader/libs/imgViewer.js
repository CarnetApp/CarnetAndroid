/*
 * imgViewer
 * 
 *
 * Copyright (c) 2013 Wayne Mogg
 * Licensed under the MIT license.
 */

var waitForFinalEvent = (function () {
	var timers = {};
	return function (callback, ms, uniqueId) {
		if (!uniqueId) {
			uniqueId = "Don't call this twice without a uniqueId";
		}
		if (timers[uniqueId]) {
			clearTimeout (timers[uniqueId]);
		}
		timers[uniqueId] = setTimeout(callback, ms);
	};
})();
/*
 *	imgViewer plugin starts here
 */ 
;(function($) {
	$.widget("wgm.imgViewer", {
		options: {
			zoomStep: 0.1,
			zoom: 1,
			zoomMax: undefined,
			zoomable: true,
			dragable: true,
			onReady: $.noop,
			onClick: $.noop,
			onUpdate: $.noop
		},
		
		_create: function() {
			var self = this;
			if (!this.element.is("img")) {
				$.error('imgviewer plugin can only be applied to img elements');
			}
//		the original img element
			self.img = self.element[0];
			var $img = $(self.img);
/*
 *		a copy of the original image to be positioned over it and manipulated to
 *		provide zoom and pan
 */
			self.zimg = $("<img />", {"src": self.img.src}).appendTo("#fullimg_container").wrap("<div class='viewport' />");
			var $zimg = $(self.zimg);
//		the container or viewport for the image view
			self.view = $(self.zimg).parent();
			var $view = $(self.view);
//		the pixel coordinate of the original image at the center of the viewport
			self.vCenter = {};
//		a flag used to decide if a mouse click is part of a drag or a proper click
			self.drag = false;
			self.pinch = false;
//		a flag used to check the target image has loaded
			self.ready = false;
			$img.one("load",function() {
//			get and some geometry information about the image
				self.ready = true;
				var	width = $img.width(),
					height = $img.height(),
					offset = $img.offset();
//			cache the image padding information
					self.offsetPadding = {
							top: parseInt($img.css('padding-top'),10),
							left: parseInt($img.css('padding-left'),10),
							right: parseInt($img.css('padding-right'),10),
							bottom: parseInt($img.css('padding-bottom'),10)
					};
/*
 *			cache the image margin/border size information
 *			because of IE8 limitations left and right borders are assumed to be the same width 
 *			and likewise top and bottom borders
 */
					self.offsetBorder = {
							x: Math.round(($img.outerWidth()-$img.innerWidth())/2),
							y: Math.round(($img.outerHeight()-$img.innerHeight())/2)
					};
/*
 *			define the css style for the view container using absolute positioning to
 *			put it directly over the original image
 */
					var vTop = offset.top + self.offsetBorder.y + self.offsetPadding.top,
						vLeft = offset.left + self.offsetBorder.x + self.offsetPadding.left;

					$view.css({
								position: "absolute",
								overflow: "hidden",
								top:  50+"%",
								left: 50+"%",
								marginLeft:-width/2+"px",
								marginTop:-height/2+"px",								
								
					});
//			the zoom and pan image is position relative to the view container
					$zimg.css({
								position: "relative",
								top: 0+"px",
								left: 0+"px",
								width: width+"px",
								height: height+"px",
								"-webkit-tap-highlight-color": "transparent"
					});
//			the initial view is centered at the orignal image
					self.vCenter = {
									x: width/2,
									y: height/2
					};
					self.update();
			}).each(function() {
				if (this.complete) { $(this).trigger("load"); }
			});
/*
 *			Render loop code during dragging and scaling using requestAnimationFrame
 */
			self.render = false;
/*
 *		Event handlers
 */
			$zimg.hammer();
			
			if (self.options.zoomable) {
				self._bind_zoom_events();
			}
			if (self.options.dragable) {
				self._bind_drag_events();
			}
			$zimg.on("tap", function(ev) {
				ev.preventDefault();
				if (!self.dragging) {
					var scoff = self._get_scroll_offset();
					ev.pageX = ev.gesture.center.x + scoff.x;
					ev.pageY = ev.gesture.center.y + scoff.y;
					self.options.onClick.call(self, ev);
				}
			});			
/*
 *		Window resize handler
 */
	
			$(window).resize(function() {
				self._view_resize();
				waitForFinalEvent(function(){
					self._view_resize();
				}, 300, $img[0].id);
				
			});
			self._view_resize();

			self.options.onReady.call(self);
		},
/*
 *	Return the window scroll offset - required to convert Hammer.js event coords to page locations
 */
		_get_scroll_offset: function() {
			var sx,sy;
			if (window.scrollX === undefined) {
				if (window.pageXOffset === undefined) {
					sx = document.documentElement.scrollLeft;
					sy = document.documentElement.scrollTop;
				} else {
					sx = window.pageXOffset;
					sy = window.pageYOffset;
				}
			} else {
				sx = window.scrollX;
				sy = window.scrollY;
			}
			return {x: sx, y: sy};
		},
/*
 *	View resize - the aim is to keep the view centered on the same location in the original image
 */
		_view_resize: function() {
			if (this.ready) {
				var $view = $(this.view),
					$img = $(this.img),
					width = $img.width(),
					height = $img.height(),
					offset = $img.offset(),
					vTop = Math.round(offset.top + this.offsetBorder.y + this.offsetPadding.top),
					vLeft = Math.round(offset.left + this.offsetBorder.x + this.offsetPadding.left);
				this.vCenter.x *=$img.width()/$view.width();
				this.vCenter.y *= $img.height()/$view.height(); 
				$view.css({
					top:  50+"%",
					left: 50+"%",
				});

				this.update();
			}
		},
/*
 *	Bind events
 */
		_bind_zoom_events: function() {
			var self = this;
			var $zimg = $(self.zimg);

			function doRender() {
				if (self.render) {
					window.requestAnimationFrame(doRender);
					self.update();
				}
			}
			function startRenderLoop() {
				if (!self.render) {
					self.render = true;
					doRender();
				}
			}
			function stopRenderLoop() {
				self.render = false;
			}

			$zimg.on("mousewheel", function(ev) {
                console.log("wheel")
					ev.preventDefault();
					var delta = ev.deltaY ;
					self.options.zoom -= delta * self.options.zoomStep;
					self.update();
			});

			$zimg.on("touchmove", function(e) {
				e.preventDefault();
//				e.stopPropagation();
			});
			$zimg.data("hammer").recognizers[1].options.enable = true;
			
			$zimg.on("pinchstart", function() {
			});
			
			$zimg.on("pinch", function(ev) {
				ev.preventDefault();
				if (!self.pinch) {
					var scoff = self._get_scroll_offset();
					self.pinchstart = { x: ev.gesture.center.x+scoff.x, y: ev.gesture.center.y+scoff.y};
					self.pinchstartrelpos = self.cursorToImg(self.pinchstart.x, self.pinchstart.y);
					self.pinchstart_scale = self.options.zoom;
					startRenderLoop();
					self.pinch = true;
				} else {
					self.options.zoom = ev.gesture.scale * self.pinchstart_scale;
					var npos = self.imgToCursor( self.pinchstartrelpos.x, self.pinchstartrelpos.y);
					self.vCenter.x = self.vCenter.x + (npos.x - self.pinchstart.x)/self.options.zoom;
					self.vCenter.y = self.vCenter.y + (npos.y - self.pinchstart.y)/self.options.zoom;
				}
			});

			$zimg.on("pinchend", function(ev) {
				ev.preventDefault();
				if (self.pinch) {
					stopRenderLoop();
					self.update();
					self.pinch = false;
				}
			});
		},
		
		_bind_drag_events: function() {
			var self = this;
			var $zimg = $(self.zimg);
			function doRender() {
				if (self.render) {
					window.requestAnimationFrame(doRender);
					self.update();
				}
			}
			function startRenderLoop() {
				if (!self.render) {
					self.render = true;
					doRender();
				}
			}
			function stopRenderLoop() {
				self.render = false;
			}
			$zimg.on("mousedown", function(e) {
				e.preventDefault();
			});
			$zimg.on("panstart", function() {
			});

			$zimg.on("panmove", function(ev) {
				ev.preventDefault();
				if (!self.drag) {
					self.drag = true;
					self.dragXorg = self.vCenter.x;
					self.dragYorg = self.vCenter.y;
					startRenderLoop();
				} else {
					self.vCenter.x = self.dragXorg - ev.gesture.deltaX/self.options.zoom;
					self.vCenter.y = self.dragYorg - ev.gesture.deltaY/self.options.zoom;
				}
			});

			$zimg.on( "panend", function(ev) {
				ev.preventDefault();
				if (self.drag) {
					self.drag = false;
					stopRenderLoop();
					self.update();
				}
			});
		},
/*
 *	Unbind events
 */
		_unbind_zoom_events: function() {
			var self = this;
			var $zimg = $(self.zimg);
			$zimg.data("hammer").recognizers[1].options.enable = false;
			$zimg.off("mousewheel");
			$zimg.off("pinchstart");
			$zimg.off("pinch");
			$zimg.off("pinchend");
		},	

		_unbind_drag_events: function() {
			var self = this;
			var $zimg = $(self.zimg);
			$zimg.off("pan");
			$zimg.off("panend");
		},	

/*
 *	Remove the plugin
 */  
		destroy: function() {
			var $zimg = $(this.zimg);
			$zimg.unbind("click");
			$(window).unbind("resize");
			$zimg.remove();
			$(this.view).remove();
			$.Widget.prototype.destroy.call(this);
		},
  
		_setOption: function(key, value) {
			switch(key) {
				case 'zoom':
					if (parseFloat(value) < 1 || isNaN(parseFloat(value))) {
						return;
					}
					break;
				case 'zoomStep':
					if (parseFloat(value) <= 0 ||  isNaN(parseFloat(value))) {
						return;
					}
					break;
				case 'zoomMax':
					if (parseFloat(value) < 1 || isNaN(parseFloat(value))) {
						return;
					}
					break;
			}
			var version = $.ui.version.split('.');
			if (version[0] > 1 || version[1] > 8) {
				this._super(key, value);
			} else {
				$.Widget.prototype._setOption.apply(this, arguments);
			}
			switch(key) {
				case 'zoom':
					if (this.ready) {
						this.update();
					}
					break;
				case 'zoomable':
					if (this.options.zoomable) {
						this._bind_zoom_events();
					} else {
						this._unbind_zoom_events();
					}
					break;
				case 'dragable':
					if (this.options.dragable) {
						this._bind_drag_events();
					} else {
						this._unbind_drag_events();
					}
					break;
				case 'zoomMax':
					if (this.ready) {
						this._view_resize();
						this.update();
					}
					break;
			}
		},
		
		addElem: function(elem) {
			$(this.view).append(elem);
		},
/*
 *	Test if a relative image coordinate is visible in the current view
 */
		isVisible: function(relx, rely) {
			var view = this.getView();
			if (view) {
				return (relx >= view.left && relx <= view.right && rely >= view.top && rely <= view.bottom);
			} else {
				return false;
			}
		},
/*
 *	Get relative image coordinates of current view
 */
		getView: function() {
			if (this.ready) {
				var $img = $(this.img),
					width = $img.width(),
					height = $img.height(),
					zoom = this.options.zoom;
				return {
					top: this.vCenter.y/height - 0.5/zoom,
					left: this.vCenter.x/width - 0.5/zoom,
					bottom: this.vCenter.y/height + 0.5/zoom,
					right: this.vCenter.x/width + 0.5/zoom
				};
			} else {
				return null;
			}
		},
/*
 *	Pan the view to be centred at the given relative image location
 */
		panTo: function(relx, rely) {
			if ( this.ready && relx >= 0 && relx <= 1 && rely >= 0 && rely <=1 ) {
				var $img = $(this.img),
					width = $img.width(),
					height = $img.height();
				this.vCenter.x = relx * width;
				this.vCenter.y = rely * height;
				this.update();
				return { x: this.vCenter.x/width, y: this.vCenter.y/height };
			} else {
				return null;
			}
		},
/*
 *	Convert a relative image location to a viewport pixel location
 */  
		imgToView: function(relx, rely) {
			if ( this.ready && relx >= 0 && relx <= 1 && rely >= 0 && rely <=1 ) {
				var $img = $(this.img),
					width = $img.width(),
					height = $img.height();						
			 
				var zLeft = width/2 - this.vCenter.x * this.options.zoom;
				var zTop =  height/2 - this.vCenter.y * this.options.zoom;
				var vx = relx * width * this.options.zoom + zLeft;
				var vy = rely * height * this.options.zoom + zTop;
				return { x: Math.round(vx), y: Math.round(vy) };
			} else {						
				
				return null;
			}
		},
/*
 *	Convert a relative image location to a page pixel location
 */  
		imgToCursor: function(relx, rely) {
			var pos = this.imgToView(relx, rely);
			if (pos) {
				var offset = $(this.img).offset();
				pos.x += offset.left + this.offsetBorder.x + this.offsetPadding.left;
				pos.y += offset.top + this.offsetBorder.y + this.offsetPadding.top;
				return pos;
			} else {
				return null;
			}
		},
/*
 *	Convert a viewport pixel location to a relative image coordinate
 */		
		viewToImg: function(vx, vy) {
			if (this.ready) {
				var $img = $(this.img),
					width = $img.width(),
					height = $img.height();
				var zLeft = width/2 - this.vCenter.x * this.options.zoom;
				var zTop =  height/2 - this.vCenter.y * this.options.zoom;
				var relx= (vx - zLeft)/(width * this.options.zoom);
				var rely = (vy - zTop)/(height * this.options.zoom);
				if (relx>=0 && relx<=1 && rely>=0 && rely<=1) {
					return {x: relx, y: rely};
				} else {
					return null;
				}
			} else {
				return null;
			}
		},
		
/*
 *	Convert a page pixel location to a relative image coordinate
 */		
		cursorToImg: function(cx, cy) {
			if (this.ready) {
				var $img = $(this.img),
					width = $img.width(),
					height = $img.height(),
					offset = $img.offset();
				var zLeft = width/2 - this.vCenter.x * this.options.zoom;
				var zTop =  height/2 - this.vCenter.y * this.options.zoom;
				var relx = (cx - offset.left - this.offsetBorder.x - this.offsetPadding.left- zLeft)/(width * this.options.zoom);
				var rely = (cy - offset.top - this.offsetBorder.y - this.offsetPadding.top - zTop)/(height * this.options.zoom);
				if (relx>=0 && relx<=1 && rely>=0 && rely<=1) {
					return {x: relx, y: rely};
				} else {
					return null;
				}
			} else {
				return null;
			}
		},
/*
 * Convert relative image coordinate to Image pixel
 */
		relposToImage: function(pos) {
			if (this.ready) {
				var img = this.img,
					width = img.naturalWidth,
					height = img.naturalHeight;
				return {x: Math.round(pos.x*width), y: Math.round(pos.y*height)};
			} else {
				return null;
			}
		},
/*
 *	Adjust the display of the image  
 */
		update: function() {
			if (this.ready) {
				var zTop, zLeft, zWidth, zHeight,
					$img = $(this.img),
					width = $img.width(),
					height = $img.height(),
//					offset = $img.offset(),
					zoom = this.options.zoom,
					zoomMax = this.options.zoomMax,
					half_width = width/2,
					half_height = height/2;
  
				zoom = zoomMax === undefined ? zoom : Math.min(zoom, zoomMax);
				this.options.zoom = zoom;

				if (zoom <= 1) {
					zTop = 0;
					zLeft = 0;
					zWidth = width;
					zHeight = height;
					this.vCenter = { 
									x: half_width,
									y: half_height
					};
					this.options.zoom = 1;
					zoom = 1;
				} else {
					zTop = Math.round(half_height - this.vCenter.y * zoom);
					zLeft = Math.round(half_width - this.vCenter.x * zoom);
					zWidth = Math.round(width * zoom);
					zHeight = Math.round(height * zoom);
/*
 *			adjust the view center so the image edges snap to the edge of the view
 */
					if (zLeft > 0) {
						this.vCenter.x = half_width/zoom;
						zLeft = 0;
					} else if (zLeft+zWidth < width) {
						this.vCenter.x = width - half_width/zoom ;
						zLeft = width - zWidth;
					}
					if (zTop > 0) {
						this.vCenter.y = half_height/zoom;
						zTop = 0;
					} else if (zTop + zHeight < height) {
						this.vCenter.y = height - half_height/zoom;
						zTop = height - zHeight;
					}
				}
				$(this.zimg).css({
								width: zoom*width+"px",
								height: zoom*height+"px"
				});

				var xt = -(this.vCenter.x - half_width)*zoom;
				var yt = -(this.vCenter.y - half_height)*zoom;
				$(this.zimg).css({transform: "translate(" + xt + "px," + yt + "px) scale(" + 1 + "," + 1 + ")" });
				this.view.css({
					marginTop:-zoom*height/2+"px",					
					marginLeft:-zoom*width/2+"px",
				
		});
/*
 *		define the onUpdate option to do something after the image is redisplayed
 *		probably shouldn't pass out the this object - need to think of something better
 */
				this.options.onUpdate.call(this);
			}
		}
	});
})(jQuery);