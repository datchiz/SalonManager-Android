package com.framgia.fsalon.screen.booking.detail;

import com.framgia.fsalon.BasePresenter;
import com.framgia.fsalon.BaseViewModel;
import com.framgia.fsalon.data.model.BookingOder;

/**
 * This specifies the contract between the view and the presenter.
 */
interface DetailContract {
    /**
     * View.
     */
    interface ViewModel extends BaseViewModel<Presenter> {
        void onGetBookingError(String msg);
        void onGetBookingSuccess(BookingOder bookingOder);
        void onBookingEmpty();
        void onNotLogin();
        void finishRefresh();
    }

    /**
     * Presenter.
     */
    interface Presenter extends BasePresenter {
        void getBookingByPhone();
    }
}