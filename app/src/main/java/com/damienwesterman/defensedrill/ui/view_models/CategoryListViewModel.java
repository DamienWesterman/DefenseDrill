/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO doc comments
public class CategoryListViewModel extends AndroidViewModel
        implements AbstractCategoryListViewModel {
    private final MutableLiveData<List<AbstractCategoryEntity>> categories;
    private final DrillRepository repo;
    private final ExecutorService executors = Executors.newSingleThreadExecutor();

    public CategoryListViewModel(@NonNull Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        categories = new MutableLiveData<>();
    }

    @Override
    public LiveData<List<AbstractCategoryEntity>> getAbstractCategories() {
        return categories;
    }

    @Override
    public void populateAbstractCategories() {
        if (null == categories.getValue()) {
            rePopulateAbstractCategories();
        }
    }

    @Override
    public void rePopulateAbstractCategories() {
        executors.execute(() -> categories.postValue(new ArrayList<>(repo.getAllCategories())));
    }

    @Override
    public void deleteAbstractCategory() {

    }

    @Override
    public void saveAbstractEntity(AbstractCategoryEntity entity, CreateNewEntityCallback callback) {

    }

    @Override
    public void updateAbstractEntity(AbstractCategoryEntity entity) {

    }

    @Override
    public AbstractCategoryEntity findById(long id) {
        return null;
    }
}
