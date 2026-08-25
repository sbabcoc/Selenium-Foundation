var body = document.getElementsByTagName('body')[0];
var container = body.querySelector('div#optional-component-div');
var root = container.getElementsByTagName('component');
if (!root.length) {
    var newRoot = document.createElement('component');
    newRoot.id = 'optional-component';
    var child = document.createElement('span');
    child.id = 'optional-component-child';
    child.textContent = "I'm the optional component's child";
    newRoot.appendChild(child);
    container.appendChild(newRoot);
    return true;
} else {
    container.removeChild(root[0]);
    return false;
}
