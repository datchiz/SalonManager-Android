package com.framgia.fsalon.screen.customer;

import com.framgia.fsalon.data.model.CustomerResponse;
import com.framgia.fsalon.data.source.CustomerRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.framgia.fsalon.utils.Constant.ApiParram.FIRST_PAGE;

/**
 * Listens to user actions from the UI ({@link CustomerFragment}), retrieves the data and updates
 * the UI as required.
 */
public class CustomerPresenter implements CustomerContract.Presenter {
    private final CustomerContract.ViewModel mViewModel;
    private static final int PER_PAGE = 10;
    private CustomerRepository mCustomerRepository;
    private int mPage = FIRST_PAGE;
    private CompositeDisposable mCompositeDisposable;

    public CustomerPresenter(CustomerContract.ViewModel viewModel,
                             CustomerRepository customerRepository) {
        mViewModel = viewModel;
        mCustomerRepository = customerRepository;
        mCompositeDisposable = new CompositeDisposable();
        getCustomers(FIRST_PAGE);
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
        mCompositeDisposable.clear();
    }

    @Override
    public void getCustomers(int page) {
        Disposable disposable = mCustomerRepository.getAllCustomers(page, PER_PAGE)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(@NonNull Disposable disposable) throws Exception {
                    mViewModel.showLoadMore();
                }
            }).subscribeWith(new DisposableObserver<CustomerResponse>() {
                @Override
                public void onNext(@NonNull CustomerResponse bookingCustomers) {
                    mViewModel.onCustomersSuccessful(bookingCustomers.getData());
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    mViewModel.onCustomerFail();
                    mViewModel.hideLoadMore();
                }

                @Override
                public void onComplete() {
                    mViewModel.hideLoadMore();
                }
            });
        mCompositeDisposable.add(disposable);
        mPage = page;
    }

    @Override
    public void loadMoreData() {
        mPage++;
        getCustomers(mPage);
    }
}
