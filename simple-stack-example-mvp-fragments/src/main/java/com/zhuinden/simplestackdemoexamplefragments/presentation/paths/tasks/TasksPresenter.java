package com.zhuinden.simplestackdemoexamplefragments.presentation.paths.tasks;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.zhuinden.simplestack.Backstack;
import com.zhuinden.simplestack.Bundleable;
import com.zhuinden.simplestackdemoexamplefragments.application.Key;
import com.zhuinden.simplestackdemoexamplefragments.data.repository.TaskRepository;
import com.zhuinden.simplestackdemoexamplefragments.presentation.objects.Task;
import com.zhuinden.simplestackdemoexamplefragments.presentation.paths.addoredittask.AddOrEditTaskKey;
import com.zhuinden.simplestackdemoexamplefragments.presentation.paths.taskdetail.TaskDetailKey;
import com.zhuinden.simplestackdemoexamplefragments.util.BasePresenter;
import com.zhuinden.statebundle.StateBundle;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Owner on 2017. 01. 27..
 */
// UNSCOPED!
public class TasksPresenter
        extends BasePresenter<TasksFragment, TasksPresenter>
        implements Bundleable {
    private final Backstack backstack;
    private final TaskRepository taskRepository;
    private final Resources resources;

    @Inject
    public TasksPresenter(Backstack backstack, TaskRepository taskRepository, Resources resources) {
        this.backstack = backstack;
        this.taskRepository = taskRepository;
        this.resources = resources;
    }

    BehaviorRelay<TasksFilterType> filterType = BehaviorRelay.createDefault(TasksFilterType.ALL_TASKS);

    Disposable subscription;

    @Override
    public void onAttach(TasksFragment tasksFragment) {
        subscription = filterType //
                .doOnNext(tasksFilterType -> tasksFragment.setFilterLabelText(tasksFilterType.getFilterText())) //
                .switchMap((tasksFilterType -> tasksFilterType.filterTask(taskRepository))) //
                .observeOn(Schedulers.computation())
                .map(tasks -> tasksFragment.calculateDiff(tasks))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pairOfDiffResultAndTasks -> {
                    if(tasksFragment != null) {
                        tasksFragment.showTasks(pairOfDiffResultAndTasks, filterType.getValue());
                    }
                });
    }

    @Override
    public void onDetach(TasksFragment Fragment) {
        subscription.dispose();
    }

    public void openAddNewTask() {
        TasksFragment tasksFragment = getFragment();
        Key parentKey = tasksFragment.getKey();
        backstack.goTo(AddOrEditTaskKey.create(parentKey));
    }

    public void openTaskDetails(Task task) {
        backstack.goTo(TaskDetailKey.create(task.id()));
    }

    public void completeTask(Task task) {
        taskRepository.setTaskCompleted(task);
        getFragment().showTaskMarkedComplete();
    }

    public void uncompleteTask(Task task) {
        taskRepository.setTaskActive(task);
        getFragment().showTaskMarkedActive();
    }

    public void deleteCompletedTasks() {
        taskRepository.deleteCompletedTasks();
        getFragment().showCompletedTasksCleared();
    }

    public void setFiltering(TasksFilterType filterType) {
        this.filterType.accept(filterType);
    }

    @Override
    @NonNull
    public StateBundle toBundle() {
        StateBundle bundle = new StateBundle();
        bundle.putString("FILTERING", filterType.getValue().name());
        return bundle;
    }

    @Override
    public void fromBundle(@Nullable StateBundle bundle) {
        if(bundle != null) {
            filterType.accept(TasksFilterType.valueOf(bundle.getString("FILTERING")));
        }
    }
}
