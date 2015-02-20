//
//  ViewController.m
//  OORRAAChat
//
//  Created by Sergey Vinogradov on 19.02.15.
//  Copyright (c) 2015 Sergey Vinogradov. All rights reserved.
//

#import "ViewController.h"
#import <MQTTKit.h>

int count = 0;

#define kMQTTServerHost @"188.166.32.82"
#define outTopic @"chat/out1"
#define inTopic @"chat/in"

@interface ViewController ()

@property (nonatomic, strong) MQTTClient *clientOut;
@property (nonatomic, strong) MQTTClient *clientIn;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    NSString *clientIDOut = [UIDevice currentDevice].identifierForVendor.UUIDString;
    self.clientOut = [[MQTTClient alloc] initWithClientId:clientIDOut];

    NSString *clientIDIn = [UIDevice currentDevice].identifierForVendor.UUIDString;
    self.clientIn = [[MQTTClient alloc] initWithClientId:clientIDIn];

    [self.clientOut connectToHost:kMQTTServerHost completionHandler:^(MQTTConnectionReturnCode code) {
        if (code == ConnectionAccepted) {
//            NSLog(@"client is connected with id %@", clientIDOut);
            [self.clientOut subscribe:outTopic withCompletionHandler:^(NSArray *grantedQos) {
//                NSLog(@"subscribed to topic %@", outTopic);
            }];
        }
    }];
    
    [self.clientIn connectToHost:kMQTTServerHost completionHandler:^(MQTTConnectionReturnCode code) {
        if (code == ConnectionAccepted) {
//            NSLog(@"client is connected with id %@", clientIDIn);
            [self.clientIn subscribe:inTopic withCompletionHandler:^(NSArray *grantedQos) {
//                NSLog(@"subscribed to topic %@", inTopic);
            }];
        }
    }];
    
    [self.clientIn setMessageHandler:^(MQTTMessage *message) {
        NSString *inputText = message.payloadString;
        NSLog(@"received message %@", inputText);

        NSError *error;
        NSData *data = message.payload;

        NSMutableDictionary *dt = [NSJSONSerialization
                JSONObjectWithData:data
                options:NSJSONReadingMutableContainers|NSJSONReadingMutableLeaves
                error:&error
        ];
        
        if (error) {
            NSLog(@"%@", [error localizedDescription]);
        } else {
            NSString *a = dt[@"author"];
            NSString *t = dt[@"text"];
            NSLog(@"author=%@, text=%@", a, t);

            dispatch_async(dispatch_get_main_queue(), ^{
                [self createLabel:a andText:t andColor:[UIColor clearColor]];
            });
        }
    }];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - IBActions
- (IBAction)sendButtonAction:(UIBarButtonItem *)sender {
    [self createLabel:@"bw" andText:self.chatMessage.text andColor:[UIColor clearColor]];

    NSString *payload = [NSString stringWithFormat:@"{\"author\": \"bw\", \"text\": \"%@\"}", self.chatMessage.text];
    
    [self.clientOut publishString:payload
                       toTopic:outTopic
                        withQos:AtLeastOnce
                        retain:NO
             completionHandler:nil];
    NSLog(@"received message %@", payload);
}


- (void)dealloc {
    [self.clientOut disconnectWithCompletionHandler:^(NSUInteger code) {
        NSLog(@"MQTT is disconnected");
    }];

    [self.clientIn disconnectWithCompletionHandler:^(NSUInteger code) {
        NSLog(@"MQTT is disconnected");
    }];
}


- (void)createLabel:(NSString *)author andText:(NSString *)text andColor:(UIColor *)color {
    NSDate * now = [NSDate date];
    NSDateFormatter *outputFormatter = [[NSDateFormatter alloc] init];
    [outputFormatter setDateFormat:@"HH:mm:ss"];
    NSString *tm = [outputFormatter stringFromDate:now];
    NSLog(@"tm: %@", tm);
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(10, 50 + (count * 15), 350, 40)];
//    [label setBackgroundColor:[UIColor clearColor]];
    [label setBackgroundColor:color];
    [label setFont:[UIFont systemFontOfSize:9]];
    [label setTextAlignment:NSTextAlignmentLeft];
    [label setText:[NSString stringWithFormat:@"[%@] %@: %@", tm, author, text]];
    [[self view] addSubview:label];
    count++;
}

@end
