# DrawLandDemo
基于mapbox开发的地块勾画功能。涉及到勾画，移动，撤销，删除等功能，还有放大镜的效果。 

效果图如下：

<img src="160747.gif" width="300px"/>

将勾画部分封装成了一个DrawLandView。可以在Acitivity中调用。

```java
    <draw.land.DrawLandView
        android:id="@+id/drawLandView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

在onCreate中

```java
		drawLandView = findViewById(R.id.drawLandView);
        drawLandView.onCreate(savedInstanceState);
```

```java
 @Override
    protected void onStart() {
        super.onStart();
        drawLandView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        drawLandView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawLandView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawLandView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawLandView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        drawLandView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        drawLandView.onLowMemory();
    }
```

对外提供了三个方法：

```java
	/**
     * 加点
     */
    public void addPoint() {
        landMapView.addPoint();
    }

    /**
     * 减点
     */
    public void cancelPoint() {
        landMapView.cancelPoint();
    }

    /**
     * 获取点集合
     */
    public void getPoints() {
        landMapView.getLatLngList();
    }
```

主要是针对小米手机，用华为手机测试有问题。MirrorLandMapView显示不出来，需要进行修改。

由于mapbox的自身问题，导致MapView隐藏不掉。这里采用的是移动控件的位置，设置隐藏和显示。

```java
 private void removeMirror() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mirrorLandMapView.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(context, 1000);
        params.topMargin = -DensityUtil.dp2px(context, 500);
        mirrorLandMapView.setLayoutParams(params);
    }

    private void showMirror() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mirrorLandMapView.getLayoutParams();
        params.rightMargin = DensityUtil.dp2px(context, 10);
        params.topMargin = DensityUtil.dp2px(context, 15);
        mirrorLandMapView.setLayoutParams(params);
    }
```

如果自己有需要，可以在DrawLandView中写自己的回调方法。

```java
            @Override
            public void showMessage(boolean isClose, boolean isIntersect, String message) {
                // TODO 做自己想做的事情 
                // isClose 是否闭合, isIntersect 是否相交, message 提示文字
            }
```

如有问题，欢迎留言。