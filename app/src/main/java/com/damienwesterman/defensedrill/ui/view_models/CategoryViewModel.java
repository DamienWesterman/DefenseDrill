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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO doc comments
public class CategoryViewModel extends AbstractCategoryViewModel {
    private final MutableLiveData<List<AbstractCategoryEntity>> categories;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryViewModel(@NonNull Application application) {
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
        executor.execute(() -> categories.postValue(new ArrayList<>(repo.getAllCategories())));
    }

    @Override
    public void deleteAbstractCategory(AbstractCategoryEntity entity) {
        if (null != entity) {
            executor.execute(() -> {
                List<AbstractCategoryEntity> newCategories = categories.getValue();
                if (null != newCategories) {
                    if (CategoryEntity.class == entity.getClass()) {
                        newCategories.remove(entity);
                        CategoryEntity category = (CategoryEntity) entity;
                        categories.postValue(newCategories);
                        repo.deleteCategories(category);
                    }
                }
            });
        }
    }

    @Override
    public void saveAbstractEntity(String name, String description, @NonNull CreateNewEntityCallback callback) {
        executor.execute(() -> {
            try {
                CategoryEntity category = new CategoryEntity(name, description);
                repo.insertCategories(category);
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
                if (CategoryEntity.class == entity.getClass()) {
                    CategoryEntity category = (CategoryEntity) entity;
                    repo.updateCategories(category);
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
        List<AbstractCategoryEntity> allCategories = categories.getValue();

        if (null != allCategories) {
            for (AbstractCategoryEntity category : allCategories) {
                if (category.getId() == id) {
                    ret = category;
                    break;
                }
            }
        }

        return ret;
    }

    public static List<CategoryEntity> getCategoryList(@NonNull List<AbstractCategoryEntity> abstractCategories) {
        List<CategoryEntity> categories = new ArrayList<>(abstractCategories.size());

        for (AbstractCategoryEntity abstractCategory : abstractCategories) {
            if (abstractCategory instanceof CategoryEntity) {
                categories.add((CategoryEntity) abstractCategory);
            }
        }

        return categories;
    }
}
