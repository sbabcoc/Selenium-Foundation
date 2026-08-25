var body = document.getElementsByTagName('body')[0];
var container = body.querySelector('div#optional-frame-div');
var frame = container.getElementsByTagName('iframe');
if (!frame.length) {
    var newFrame = document.createElement('iframe');
    newFrame.id = 'frame-e';
    container.appendChild(newFrame);
    newFrame.src = '/grid/admin/FrameE_Servlet';
    return true;
} else {
    container.removeChild(frame[0]);
    return false;
}
