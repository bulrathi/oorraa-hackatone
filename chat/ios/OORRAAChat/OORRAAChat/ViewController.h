//
//  ViewController.h
//  OORRAAChat
//
//  Created by Sergey Vinogradov on 19.02.15.
//  Copyright (c) 2015 Sergey Vinogradov. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UITextField *chatMessage;

@property (weak, nonatomic) IBOutlet UIBarButtonItem *sendItemButton;

@property NSInteger labelCount;

- (IBAction)sendButtonAction:(UIBarButtonItem *)sender;

@end

