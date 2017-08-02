package com.framgia.fsalon.screen.scheduler;

import android.app.FragmentManager;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.framgia.fsalon.BR;
import com.framgia.fsalon.FSalonApplication;
import com.framgia.fsalon.R;
import com.framgia.fsalon.data.model.ManageBookingResponse;
import com.framgia.fsalon.screen.scheduler.detail.BookingDetailActivity;
import com.framgia.fsalon.utils.Utils;
import com.framgia.fsalon.utils.navigator.Navigator;
import com.framgia.fsalon.wiget.StickyHeaderLayoutManager;
import com.framgia.fsalon.wiget.TopSheetBehavior;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.framgia.fsalon.data.model.BookingOder.STATUS_CANCELED;
import static com.framgia.fsalon.data.model.BookingOder.STATUS_FINISHED;
import static com.framgia.fsalon.data.model.BookingOder.STATUS_PENDING;
import static com.framgia.fsalon.data.source.remote.ManageBookingRemoteDataSource.FILTER_DAY;
import static com.framgia.fsalon.data.source.remote.ManageBookingRemoteDataSource.FILTER_SPACE;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_END_DATE;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_SELECT_DATE;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_SPACE_TIME;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_START_DATE;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_TODAY;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_TOMORROW;
import static com.framgia.fsalon.screen.scheduler.SchedulerViewModel.TabFilter.TAB_YESTERDAY;
import static com.framgia.fsalon.utils.Constant.ApiParram.FIRST_PAGE;
import static com.framgia.fsalon.utils.Constant.ApiParram.OUT_OF_INDEX;

/**
 * Exposes the data to be used in the Scheduler screen.
 */
public class SchedulerViewModel extends BaseObservable
    implements SchedulerContract.ViewModel, DatePickerDialog.OnDateSetListener {
    private SchedulerContract.Presenter mPresenter;
    private int mTabFilter;
    private SchedulerAdapter mAdapter;
    private Navigator mNavigator;
    private StickyHeaderLayoutManager mLayoutManager = new StickyHeaderLayoutManager();
    private boolean mIsLoadingMore = false;
    private Calendar mCalendar;
    private FragmentManager mFragmentManager;
    private int mStartDate = Utils.createTimeStamp(SchedulerViewModel.TabFilter.TAB_TODAY);
    private int mEndDate = OUT_OF_INDEX;
    private DatePickerDialog mDatePickerDialog;
    private int mTypeSelectDate;
    private String mTitleTopSheet;
    private String mFilterChoice = FILTER_DAY;
    private String mSpaceTime = "";
    private int mSatus;
    private RecyclerView.OnScrollListener mScrollListenner = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy <= 0) {
                return;
            }
            int totalItems = mLayoutManager.getItemCount();
            int visibleItems = mLayoutManager.getChildCount();
            View lastVisibleItem = mLayoutManager.getBottommostChildView();
            int lastItemsVisible =
                mLayoutManager.getViewAdapterPosition(lastVisibleItem);
            if (!mIsLoadingMore && (visibleItems + lastItemsVisible) >= totalItems) {
                mPresenter.loadMoreData();
            }
        }
    };
    private Spinner.OnItemSelectedListener mSpinnerSelectListener =
        new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.clear();
                switch (i) {
                    case STATUS_CANCELED:
                        mSatus = STATUS_CANCELED;
                        mPresenter.getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX,
                            STATUS_CANCELED, mStartDate, mEndDate);
                        break;
                    case STATUS_PENDING:
                        mSatus = STATUS_PENDING;
                        mPresenter.getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX,
                            STATUS_PENDING, mStartDate, mEndDate);
                        break;
                    case STATUS_FINISHED:
                        mSatus = STATUS_FINISHED;
                        mPresenter.getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX,
                            STATUS_FINISHED, mStartDate, mEndDate);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

    public SchedulerViewModel(SchedulerFragment fragment, FragmentManager fragmentManager) {
        setTabFilter(TAB_TODAY);
        mNavigator = new Navigator(fragment);
        mFragmentManager = fragmentManager;
        setLayoutManager(mLayoutManager);
        setAdapter(new SchedulerAdapter(this, new ArrayList<ManageBookingResponse>()));
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mDatePickerDialog =
            DatePickerDialog.newInstance(this, mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.setOnDateSetListener(this);
        setTitleTopSheet(FSalonApplication.getInstant().getResources()
            .getString(R.string.action_pick_date));
    }

    @Override
    public void onStart() {
        mPresenter.onStart();
    }

    @Override
    public void onStop() {
        mPresenter.onStop();
    }

    @Override
    public void setPresenter(SchedulerContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Bindable
    public int getTabFilter() {
        return mTabFilter;
    }

    public void setTabFilter(@TabFilter int tabFilter) {
        mTabFilter = tabFilter;
        notifyPropertyChanged(BR.tabFilter);
    }

    @Override
    public void onItemFilterClick(@TabFilter int tab, View topSheet) {
        setTabFilter(tab);
        mAdapter.clear();
        switch (tab) {
            case TAB_TODAY:
                mFilterChoice = FILTER_DAY;
                mEndDate = OUT_OF_INDEX;
                mStartDate = Utils.createTimeStamp(tab);
                setTitleTopSheet(FSalonApplication.getInstant()
                    .getResources().getString(R.string.title_today));
                mPresenter
                    .getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX,
                        mSatus, mStartDate, OUT_OF_INDEX);
                break;
            case TAB_TOMORROW:
                mEndDate = OUT_OF_INDEX;
                mFilterChoice = FILTER_DAY;
                mStartDate = Utils.createTimeStamp(tab);
                setTitleTopSheet(FSalonApplication.getInstant()
                    .getResources().getString(R.string.title_tomorrow));
                mPresenter
                    .getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX,
                        mSatus, mStartDate, OUT_OF_INDEX);
                break;
            case TAB_YESTERDAY:
                mEndDate = OUT_OF_INDEX;
                mFilterChoice = FILTER_DAY;
                mStartDate = Utils.createTimeStamp(tab);
                setTitleTopSheet(FSalonApplication.getInstant()
                    .getResources().getString(R.string.title_yesterday));
                mPresenter
                    .getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX,
                        mSatus, mStartDate, OUT_OF_INDEX);
                break;
            case TAB_SELECT_DATE:
                mEndDate = OUT_OF_INDEX;
                mFilterChoice = FILTER_DAY;
                mTypeSelectDate = TAB_SELECT_DATE;
                showDatePickerDialog();
                break;
            case TAB_SPACE_TIME:
                mFilterChoice = FILTER_SPACE;
                mTypeSelectDate = TAB_START_DATE;
                showDatePickerDialog();
                break;
            default:
                break;
        }
        TopSheetBehavior.from(topSheet).setState(TopSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onSchedulerSuccessful(List<ManageBookingResponse> sections) {
        mAdapter.updateData(sections);
    }

    @Override
    public void onSchedulerFail() {
        mNavigator.showToast(R.string.title_scheduler_failure);
    }

    @Override
    public void showLoadMore() {
        setLoadingMore(true);
    }

    @Override
    public void hideLoadMore() {
        setLoadingMore(false);
    }

    @Override
    public void onClickTopSheet(View topSheet) {
        TopSheetBehavior.from(topSheet).setState(TopSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onBookingItemClick(int id) {
        mNavigator.startActivity(BookingDetailActivity.getInstance(mNavigator.getContext(), id));
    }

    @Bindable
    public SchedulerAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(SchedulerAdapter adapter) {
        mAdapter = adapter;
        notifyPropertyChanged(BR.adapter);
    }

    @Bindable
    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public void setLayoutManager(StickyHeaderLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        notifyPropertyChanged(BR.layoutManager);
    }

    @Bindable
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    public void setLoadingMore(boolean loadingMore) {
        mIsLoadingMore = loadingMore;
        notifyPropertyChanged(BR.loadingMore);
    }

    @Bindable
    public RecyclerView.OnScrollListener getScrollListenner() {
        return mScrollListenner;
    }

    @Bindable
    public String getTitleTopSheet() {
        return mTitleTopSheet;
    }

    public void setTitleTopSheet(String titleTopSheet) {
        mTitleTopSheet = titleTopSheet;
        notifyPropertyChanged(BR.titleTopSheet);
    }

    public Spinner.OnItemSelectedListener getSpinnerSelectListener() {
        return mSpinnerSelectListener;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (mDatePickerDialog != null) mDatePickerDialog.dismiss();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);
        switch (mTypeSelectDate) {
            case TAB_START_DATE:
                mStartDate = (int) (mCalendar.getTimeInMillis() / 1000);
                mTypeSelectDate = TAB_END_DATE;
                mDatePickerDialog = DatePickerDialog.newInstance(this, year,
                    monthOfYear, dayOfMonth);
                mSpaceTime = mSpaceTime.concat(Utils.convertDate(mCalendar.getTime()));
                showDatePickerDialog();
                return;
            case TAB_SELECT_DATE:
                setTitleTopSheet(Utils.convertDate(mCalendar.getTime()));
                mStartDate = (int) (mCalendar.getTimeInMillis() / 1000);
                mPresenter
                    .getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX, mSatus,
                        mStartDate, mEndDate);
                break;
            case TAB_END_DATE:
                mEndDate = (int) (mCalendar.getTimeInMillis() / 1000);
                mPresenter
                    .getSchedulers(mFilterChoice, FIRST_PAGE, OUT_OF_INDEX, mSatus,
                        mStartDate, mEndDate);
                mSpaceTime = mSpaceTime.concat(" - " + Utils.convertDate(mCalendar.getTime()));
                setTitleTopSheet(mSpaceTime);
            default:
                break;
        }
    }

    /**
     * Anotation for filter tab follow days
     */
    @IntDef({TAB_TODAY, TAB_YESTERDAY, TAB_TOMORROW, TAB_SPACE_TIME, TAB_SELECT_DATE})
    @Target(ElementType.PARAMETER)
    public @interface TabFilter {
        int TAB_TODAY = 0;
        int TAB_YESTERDAY = 1;
        int TAB_TOMORROW = 2;
        int TAB_SPACE_TIME = 3;
        int TAB_SELECT_DATE = 4;
        int TAB_START_DATE = 5;
        int TAB_END_DATE = 6;
    }

    private void showDatePickerDialog() {
        switch (mTypeSelectDate) {
            case TAB_SELECT_DATE:
                mDatePickerDialog.setTitle(FSalonApplication.getInstant().getResources()
                    .getString(R.string.title_select_date));
                break;
            case TAB_START_DATE:
                mDatePickerDialog.setTitle(FSalonApplication.getInstant().getResources()
                    .getString(R.string.title_select_start_day));
                break;
            case TAB_END_DATE:
                mDatePickerDialog.setTitle(FSalonApplication.getInstant().getResources()
                    .getString(R.string.title_select_end_day));
                break;
            default:
                break;
        }
        mDatePickerDialog.show(mFragmentManager, "");
    }
}
