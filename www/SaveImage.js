exports.saveImageToGallery = function (localImagePath, title, successCallback, failureCallback) {
    if (typeof successCallback != 'function') {
        throw new Error('ImageSaver Error: successCallback is not a function');
    }

    if (typeof failureCallback != 'function') {
        throw new Error('ImageSaver Error: failureCallback is not a function');
    }

    return cordova.exec(
        successCallback, failureCallback, 'ImageSaver', 'saveImageToGallery', [_getLocalImagePathWithoutPrefix(), title]);

    function _getLocalImagePathWithoutPrefix() {
        if (localImagePath.indexOf('file:///') === 0) {
            return localImagePath.substring(7);
        }
        return localImagePath;
    }
};

exports.saveVideoToGallery = function (localVideoPath, title, successCallback, failureCallback) {
    if (typeof successCallback != 'function') {
        throw new Error('ImageSaver Error: successCallback is not a function');
    }

    if (typeof failureCallback != 'function') {
        throw new Error('ImageSaver Error: failureCallback is not a function');
    }

    return cordova.exec(
        successCallback, failureCallback, 'ImageSaver', 'saveVideoToGallery', [_getLocalImagePathWithoutPrefix(), title]);

    function _getLocalImagePathWithoutPrefix() {
        if (localVideoPath.indexOf('file:///') === 0) {
            return localVideoPath.substring(7);
        }
        return localVideoPath;
    }
};