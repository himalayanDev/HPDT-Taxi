package com.base.utilimport io.reactivex.Schedulerimport io.reactivex.android.schedulers.AndroidSchedulersimport io.reactivex.schedulers.Schedulers/** * Created by arischoice on 20/1/2019. */class AppSchedulerProvider : SchedulerProvider {    override fun ui(): Scheduler = AndroidSchedulers.mainThread()    override fun computation(): Scheduler = Schedulers.computation()    override fun io(): Scheduler = Schedulers.io()}