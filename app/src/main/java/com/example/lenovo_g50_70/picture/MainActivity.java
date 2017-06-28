package com.example.lenovo_g50_70.picture;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private ViewPager mPager;
    private int[] mImage={R.drawable.img1,R.drawable.img2,R.drawable.img3};
    private ImageView[] mImageViews=new ImageView[mImage.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mPager.setAdapter(new PagerAdapter() {

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImageViews[position]);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ZoomImageView imageView =new ZoomImageView(getApplicationContext());
                imageView.setImageResource(mImage[1]);
                container.addView(imageView);
                mImageViews[position]=imageView;
                return imageView;
            }

            @Override
            public int getCount() {
                return mImageViews.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }
        });
    }
}
