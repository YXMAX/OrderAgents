<h3 align='center'>OrderAgents 取餐码识别</h3>

包名（`applicationId`）：`com.yxmax.orderagents`

## 功能

1. 支持以下应用的自动OCR识别:
- 微信(`com.tencent.mm`),
- 支付宝(`com.eg.android.AlipayGphone`),
- 麦当劳(`com.mcdonalds.gma.cn`),
- 肯德基(`com.yek.android.kfc.activitys`),
- 瑞幸咖啡(`com.lucky.luckyclient`)

2. 其余应用可通过 生成快捷方式 并放入Flyme的应用小窗中进行快捷的截图识别
3. 识别OCR文字特征和取餐码特征 显示对应的品牌
4. 现已支持以下品牌的识别和图片展示:
- 快餐品牌:
  - 塔斯汀
  - 麦当劳
  - 肯德基
  - 汉堡王
- 奶茶品牌:
  - 喜茶
  - 茶百道
  - 霸王茶姬
  - 沪上阿姨
  - 茶理宜世
  - 蜜雪冰城
  - CoCo
  - 库迪咖啡
  - 瑞幸咖啡
  - 一点点
  - 爷爷不泡茶
  - 古茗
  - Manner
  - 茉莉奶白
 
更多功能期待各位提出...

## 使用

1. 安装APP后, 点击右上角的 `设置` 按钮, 按照顺序给予权限并打开 `无障碍服务`
2. 有需要的可以`创建快捷方式`至桌面 并在Flyme应用小窗的功能页中添加该APP的截图识别快捷方式

## 致谢

- 感谢 [Flyme-Live-Notification-Demo](https://github.com/Ruyue-Kinsenka/Flyme-Live-Notification-Demo) 提供的 Flyme 实况通知调用参考.
- 感谢 [Pinme](https://github.com/BryceWG/Pinme) 提供的 快捷方式和OCR识别 方案参考
