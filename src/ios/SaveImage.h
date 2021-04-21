#import <Cordova/CDVPlugin.h>

@interface SaveImage : CDVPlugin {
	NSString* callbackId;
}

@property (nonatomic, copy) NSString* callbackId;

- (void)saveImageToGallery:(CDVInvokedUrlCommand*)command;
- (void)saveVideoToGallery:(CDVInvokedUrlCommand*)command;

@end
