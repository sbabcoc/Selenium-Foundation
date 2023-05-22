var body = document.getElementsByTagName('body')[0];
var formDiv = body.querySelector('div#form-div');
var optional = formDiv.getElementsByTagName('optional')
if (!optional.length) {
    var newOptional = document.createElement('optional');
    newOptional.textContent = "I'm optional";
    formDiv.appendChild(newOptional);
    return true;
} else {
    formDiv.removeChild(optional[0]);
    return false;
}
