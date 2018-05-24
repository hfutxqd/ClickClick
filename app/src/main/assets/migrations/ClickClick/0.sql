INSERT INTO defined_function (name, body, description) values ('打开百度', 'url:http://www.baidu.com/', '打开百度');
INSERT INTO defined_function (name, body, description) values ('打开QQ', 'url:mqq://', '打开QQ');
INSERT INTO defined_function (name, body, description) values ('打开微信', 'url:weixin://', '打开微信');
INSERT INTO defined_function (name, body, description) values ('打开支付宝', 'url:alipay:// ', '一键打开支付宝');
INSERT INTO defined_function (name, body, description) values ('支付宝付款', 'url:alipays://platformapi/startapp?appId=20000056', '打开支付宝付款界面');
INSERT INTO defined_function (name, body, description) values ('打开相机', 'action:android.media.action.STILL_IMAGE_CAMERA', '打开相机');
INSERT INTO defined_function (name, body, description) values ('打开录像', 'url:android.media.action.VIDEO_CAMERA', '打开录像');
INSERT INTO defined_function (name, body, description) values ('测试微信扫一扫', 'internal:wechat_scan', '打开微信扫一扫');