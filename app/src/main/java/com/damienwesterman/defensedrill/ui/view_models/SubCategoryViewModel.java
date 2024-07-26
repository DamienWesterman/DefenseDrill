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
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO doc comments
public class SubCategoryViewModel extends AndroidViewModel
        implements AbstractCategoryViewModel {
    private final MutableLiveData<List<AbstractCategoryEntity>> subCategories;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SubCategoryViewModel(@NonNull Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        subCategories = new MutableLiveData<>();
    }

    @Override
    public LiveData<List<AbstractCategoryEntity>> getAbstractCategories() {
        return subCategories;
    }

    @Override
    public void populateAbstractCategories() {
        if (null == subCategories.getValue()) {
            rePopulateAbstractCategories();
        }
    }

    public void populateAbstractCategories(long categoryId) {
        if (null == subCategories.getValue()) {
            rePopulateAbstractCategories(categoryId);
        }
    }

    @Override
    public void rePopulateAbstractCategories() {
        executor.execute(() -> subCategories.postValue(new ArrayList<>(repo.getAllSubCategories())));
    }

    public void rePopulateAbstractCategories(long categoryId) {
        executor.execute(() -> subCategories.postValue(new ArrayList<>(repo.getAllSubCategories(categoryId))));
    }

    @Override
    public void deleteAbstractCategory(AbstractCategoryEntity entity) {
        if (null != entity) {
            executor.execute(() -> {
                List<AbstractCategoryEntity> newSubCategories = subCategories.getValue();
                if (null != newSubCategories) {
                    if (SubCategoryEntity.class == entity.getClass()) {
                        newSubCategories.remove(entity);
                        SubCategoryEntity subCategory = (SubCategoryEntity) entity;
                        subCategories.postValue(newSubCategories);
                        repo.deleteSubCategories(subCategory);
                    }
                }
            });
        }
    }

    @Override
    public void saveAbstractEntity(String name, String description, @NonNull CreateNewEntityCallback callback) {
        executor.execute(() -> {
            try {
                SubCategoryEntity subCategory = new SubCategoryEntity(name, description);
                repo.insertSubCategories(subCategory);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onFailure(e.getMessage());
            }
        });
    }

    @Override
    public void updateAbstractEntity(AbstractCategoryEntity entity, @NonNull CreateNewEntityCallback callback) {
        executor.execute(() -> {
            try {
                if (SubCategoryEntity.class == entity.getClass()) {
                    SubCategoryEntity subCategory = (SubCategoryEntity) entity;
                    repo.updateSubCategories(subCategory);
                    callback.onSuccess();
                }
            } catch (SQLiteConstraintException e) {
                callback.onFailure(e.getMessage());
            }
        });
    }

    @Nullable
    @Override
    public AbstractCategoryEntity findById(long id) {
        AbstractCategoryEntity ret = null;
        List<AbstractCategoryEntity> allSubCategories = subCategories.getValue();

        if (null != allSubCategories) {
            for (AbstractCategoryEntity subCategory : allSubCategories) {
                if (subCategory.getId() == id) {
                    ret = subCategory;
                    break;
                }
            }
        }

        return ret;
    }

    public static List<SubCategoryEntity> getSubCategoryList(@NonNull List<AbstractCategoryEntity> abstractCategories) {
        List<SubCategoryEntity> subCategories = new ArrayList<>(abstractCategories.size());

        for (AbstractCategoryEntity abstractCategory : abstractCategories) {
            if (abstractCategory instanceof SubCategoryEntity) {
                subCategories.add((SubCategoryEntity) abstractCategory);
            }
        }

        return subCategories;
    }
}
