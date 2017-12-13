package com.zhuinden.simplestackdemoexamplemvp.presentation.paths.statistics;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhuinden.simplestackdemoexamplemvp.R;
import com.zhuinden.simplestackdemoexamplemvp.application.Injector;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhuinden on 2017.01.26..
 */

public class StatisticsView
        extends LinearLayout {
    public StatisticsView(Context context) {
        super(context);
        init(context);
    }

    public StatisticsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StatisticsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public StatisticsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        if(!isInEditMode()) {
            resources = Injector.get().resources();
            statisticsPresenter = Injector.get().statisticsPresenter();
        }
    }

    private Resources resources;
    private StatisticsPresenter statisticsPresenter;

    @BindView(R.id.statistics)
    TextView statisticsText;

    public void setProgressIndicator(boolean active) {
        if(active) {
            statisticsText.setText(resources.getString(R.string.loading));
        } else {
            statisticsText.setText("");
        }
    }

    public void showStatistics(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
        if(numberOfCompletedTasks == 0 && numberOfIncompleteTasks == 0) {
            statisticsText.setText(resources.getString(R.string.statistics_no_tasks));
        } else {
            String displayString = resources.getString(R.string.statistics_active_tasks) + " " + numberOfIncompleteTasks + "\n" + resources.getString(
                    R.string.statistics_completed_tasks) + " " + numberOfCompletedTasks;
            statisticsText.setText(displayString);
        }
    }

    public void showLoadingStatisticsError() {
        statisticsText.setText(resources.getString(R.string.statistics_error));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        statisticsPresenter.attachView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        statisticsPresenter.detachView(this);
        super.onDetachedFromWindow();
    }
}
