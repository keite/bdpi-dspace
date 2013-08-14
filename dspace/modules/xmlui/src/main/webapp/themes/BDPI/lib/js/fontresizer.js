    function changeFont(fontsize) {

    var fnt_small = document.getElementById('fnt_small');
    var fnt_reset = document.getElementById('fnt_reset');
    var fnt_big = document.getElementById('fnt_big');
	var fontAtual = document.defaultView.getComputedStyle(document.body, "").getPropertyValue("font-size");
	var indexOf = fontAtual.indexOf('p');
	var value = parseInt(fontAtual.substring(0,indexOf));
	
    switch (fontsize) {
    
	case 'small':
		value -= 3;
		if(value >= 10) {
			document.body.style.fontSize = value + 'px';
			SetCookie('font-size','small');
			fnt_small.style.textDecoration = 'none';
			fnt_reset.style.textDecoration = 'none';
			fnt_big.style.textDecoration = 'none';
		}
    break;
    
	case 'reset':
		document.body.style.fontSize = '80%';
		SetCookie('font-size','reset');
		fnt_small.style.textDecoration = 'none';
		fnt_reset.style.textDecoration = 'none';
		fnt_big.style.textDecoration = 'none';
    break;
	
    case 'big':
		value += 3;
		if(value <= 32) {
			document.body.style.fontSize = value + 'px';
			SetCookie('font-size','big');
			fnt_small.style.textDecoration = 'none';
			fnt_reset.style.textDecoration = 'none';
			fnt_big.style.textDecoration = 'none';
		}
	break; 
	}	
	}
    
    function getCookieVal (offset) {
    var endstr = document.cookie.indexOf (";", offset);
    if (endstr == -1)
    endstr = document.cookie.length;
    return unescape(document.cookie.substring(offset, endstr));
    }
    function GetCookie (name) {
    var arg = name + "=";
    var alen = arg.length;
    var clen = document.cookie.length;
    var i = 0;
    while (i < clen) {
    var j = i + alen;
    if (document.cookie.substring(i, j) == arg) return getCookieVal (j);
    i = document.cookie.indexOf(" ", i) + 1;
    if (i == 0) break;
    }
    return null;
    }
    function SetCookie (name, value) {
    var argv = SetCookie.arguments;
    var argc = SetCookie.arguments.length;
    var expires = (argc > 2) ? argv[2] : null;
    var path = (argc > 3) ? argv[3] : null;
    var domain = (argc > 4) ? argv[4] : null;
    var secure = (argc > 5) ? argv[5] : false;
    document.cookie = name + "=" + escape (value) +
    ((expires == null) ? "" : ("; expires=" + expires.toGMTString())) +
    ((path == null) ? "" : ("; path=" + path)) +
    ((domain == null) ? "" : ("; domain=" + domain)) +
    ((secure == true) ? "; secure" : "");
    }