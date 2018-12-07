function getCookie(cname) {
	//alert("getCookie " + cname);
    var name = cname + "=";
    //alert("document.cookie " + document.cookie);
    var ca = document.cookie.split(';');
    for(var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}
function checkCookie() {
    var user = getCookie("_cchecker");
    if (user != "") {
        alert("Welcome again " + user);
    } else {
    	alert("No user !!! ");
    }
}