package com.knosseir.admin.bruinfeed;

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add slides to intro activity
        addSlide(AppIntroFragment.newInstance("Welcome to BruinFeed!", "by Kareem Nosseir", R.mipmap.ucla_logo_cursive, getResources().getColor(R.color.colorPrimaryDark)));
        addSlide(AppIntroFragment.newInstance("BruinFeed helps you plan your next meal.", "You can search and filter items from any dining hall to pick what you want to eat!",
                R.mipmap.restaurant, getResources().getColor(R.color.colorPrimaryDark)));
        addSlide(AppIntroFragment.newInstance("BruinFeed helps you keep an eye on those calories!", "You can browse the nutrition facts, ingredients, and allergens for each item!",
                R.mipmap.healthy, getResources().getColor(R.color.colorPrimaryDark)));


        // override bar/separator color
        setBarColor(getResources().getColor(R.color.colorPrimaryDark));
        setSeparatorColor(getResources().getColor(R.color.colorPrimaryDark));

        // hide the status bar
        showStatusBar(false);

        // show skip and done buttons
        showSkipButton(true);
        showDoneButton(true);

        setFadeAnimation();
    }

    @Override
    public void onSkipPressed() {
        // do something when users tap on Skip button
        finish();
    }

    @Override
    public void onNextPressed() {
        // do something when users tap on Next button
    }

    @Override
    public void onDonePressed() {
        // do something when users tap on Done button
        finish();
    }

    @Override
    public void onSlideChanged() {
        // do something when slide is changed
    }
}