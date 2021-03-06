package com.framgia.fsalon.screen.booking.detail;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.framgia.fsalon.R;
import com.framgia.fsalon.data.source.BookingRepository;
import com.framgia.fsalon.data.source.UserRepository;
import com.framgia.fsalon.data.source.api.FSalonServiceClient;
import com.framgia.fsalon.data.source.local.UserLocalDataSource;
import com.framgia.fsalon.data.source.local.sharepref.SharePreferenceImp;
import com.framgia.fsalon.data.source.remote.BookingRemoteDataSource;
import com.framgia.fsalon.data.source.remote.UserRemoteDataSource;
import com.framgia.fsalon.databinding.FragmentDetailBinding;

/**
 * Created by THM on 8/1/2017.
 */
public class DetailFragment extends Fragment {
    private DetailContract.ViewModel mViewModel;

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new DetailViewModel(this);
        DetailContract.Presenter presenter = new DetailPresenter(mViewModel,
            new UserRepository(new UserRemoteDataSource(FSalonServiceClient.getInstance()),
                new UserLocalDataSource(new SharePreferenceImp(getContext()))),
            new BookingRepository(new BookingRemoteDataSource(FSalonServiceClient.getInstance())));
        mViewModel.setPresenter(presenter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentDetailBinding binding = DataBindingUtil.inflate(inflater, R.layout
            .fragment_detail, container, false);
        binding.setViewModel((DetailViewModel) mViewModel);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mViewModel.onStart();
    }

    @Override
    public void onStop() {
        mViewModel.onStop();
        super.onStop();
    }
}
