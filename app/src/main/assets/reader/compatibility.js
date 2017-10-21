

var isElectron = true;
if(typeof require !== "function"){

	isElectron = false;
	var require = function(required){
		if(required == "fs"){

			return new FSCompatibility();
		}	
	return "";	
	}	
}
