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
/*
 * Copyright 2024 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.ui.viewmodel;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.util.OperationCompleteCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * View model for {@link SubCategoryEntity}, geared towards a list of them and allowing CRUD
 * functionality.
 */
@HiltViewModel
public class SubCategoryViewModel extends AbstractCategoryViewModel {
    private final MutableLiveData<List<AbstractCategoryEntity>> subCategories;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public SubCategoryViewModel(@NonNull Application application, DrillRepository repo) {
        super(application);

        this.repo = repo;
        subCategories = new MutableLiveData<>();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public LiveData<List<AbstractCategoryEntity>> getAbstractCategories() {
        return subCategories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populateAbstractCategories() {
        if (null == subCategories.getValue()) {
            rePopulateAbstractCategories();
        }
    }

    /**
     * Populate our list of abstract categories if it has not already been done. Will only select
     * entities that belong to the passed category ID.
     *
     * @param categoryId Category ID to filter by.
     */
    public void populateAbstractCategories(long categoryId) {
        if (null == subCategories.getValue()) {
            rePopulateAbstractCategories(categoryId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rePopulateAbstractCategories() {
        executor.execute(() -> subCategories.postValue(new ArrayList<>(repo.getAllSubCategories())));
    }

    /**
     * Force re-load the abstract categories from the database, even if they are already loaded.
     * Will only select entities that belong to the passed category ID.
     *
     * @param categoryId Category ID to filter by.
     */
    public void rePopulateAbstractCategories(long categoryId) {
        executor.execute(() -> subCategories.postValue(new ArrayList<>(repo.getAllSubCategories(categoryId))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAbstractCategory(@NonNull AbstractCategoryEntity entity) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAbstractEntity(@NonNull String name, @NonNull String description, @NonNull OperationCompleteCallback callback) {
        executor.execute(() -> {
            try {
                SubCategoryEntity subCategory = new SubCategoryEntity(name, description);
                if (!repo.insertSubCategories(subCategory)) {
                    callback.onFailure("Something went wrong");
                } else {
                    callback.onSuccess();
                }
            } catch (SQLiteConstraintException e) {
                callback.onFailure("Name already exists");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAbstractEntity(@NonNull AbstractCategoryEntity entity, @NonNull OperationCompleteCallback callback) {
        executor.execute(() -> {
            try {
                if (SubCategoryEntity.class == entity.getClass()) {
                    SubCategoryEntity subCategory = (SubCategoryEntity) entity;
                    if (!repo.updateSubCategories(subCategory)) {
                        callback.onFailure("Something went wrong");
                    } else {
                        callback.onSuccess();
                    }
                }
            } catch (SQLiteConstraintException e) {
                callback.onFailure("Name already exists");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Static helper method to convert a list of AbstractCategoryEntity objects to a list of
     * SubCategoryEntity objects.
     *
     * @param abstractCategories    List of AbstractCategoryEntity objects.
     * @return                      Converted list of SubCategoryEntity objects.
     */
    @NonNull
    public static List<SubCategoryEntity> getSubCategoryList(@NonNull List<AbstractCategoryEntity> abstractCategories) {
        List<SubCategoryEntity> subCategories = new ArrayList<>(abstractCategories.size());

        for (AbstractCategoryEntity abstractCategory : abstractCategories) {
            if (SubCategoryEntity.class == abstractCategory.getClass()) {
                subCategories.add((SubCategoryEntity) abstractCategory);
            }
        }

        return subCategories;
    }
}
