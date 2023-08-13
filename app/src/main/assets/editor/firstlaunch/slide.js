var Slides = function (elem, onEnd) {
    this.elem = elem;
    this.slideIndex = 1;
    this.onEnd = onEnd;
    const slides = this;
    this.elem.getElementsByClassName("next")[0].onclick = function () {
        slides.plusSlides(1);
    }
    this.currentSlide(1);
}



// Next/previous controls
Slides.prototype.plusSlides = function (n) {
    this.showSlides(slideIndex += n);
}

// Thumbnail image controls
Slides.prototype.currentSlide = function (n) {
    this.showSlides(slideIndex = n);
}

Slides.prototype.showSlides = function (n) {
    var i;
    var slides = this.elem.getElementsByClassName("mySlides");
    var dots = this.elem.getElementsByClassName("dot");
    if (n > slides.length) {
        this.onEnd();
        return;
    }
    if (n < 1) {
        slideIndex = slides.length
    }
    for (i = 0; i < slides.length; i++) {
        slides[i].style.display = "none";
    }
    for (i = 0; i < dots.length; i++) {
        dots[i].className = dots[i].className.replace(" active", "");
    }
    slides[slideIndex - 1].style.display = "block";
    dots[slideIndex - 1].className += " active";
}