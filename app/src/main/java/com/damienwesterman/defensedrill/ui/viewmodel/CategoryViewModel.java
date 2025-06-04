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
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * View model for {@link CategoryEntity}, geared towards a list of them and allowing CRUD
 * functionality.
 */
@HiltViewModel
public class CategoryViewModel extends AbstractCategoryViewModel {
    private final MutableLiveData<List<AbstractCategoryEntity>> uiCategoriesList;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public CategoryViewModel(@NonNull Application application, DrillRepository repo) {
        super(application);

        this.repo = repo;
        uiCategoriesList = new MutableLiveData<>();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public LiveData<List<AbstractCategoryEntity>> getUiAbstractCategoriesList() {
        return uiCategoriesList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populateAbstractCategories() {
        if (null == uiCategoriesList.getValue()) {
            rePopulateAbstractCategories();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rePopulateAbstractCategories() {
        executor.execute(() -> uiCategoriesList.postValue(new ArrayList<>(repo.getAllCategories())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAbstractCategory(@NonNull AbstractCategoryEntity entity) {
        executor.execute(() -> {
            if (null != uiCategoriesList.getValue()
                    && CategoryEntity.class == entity.getClass()) {
                // Must be a new list to trigger submitList() logic
                List<AbstractCategoryEntity> newCategories = new ArrayList<>(uiCategoriesList.getValue());
                newCategories.remove(entity);
                uiCategoriesList.postValue(newCategories);
                repo.deleteCategories((CategoryEntity) entity);
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
                CategoryEntity category = new CategoryEntity(name, description);
                if(!repo.insertCategories(category)) {
                    callback.onFailure("Something went wrong");
                } else {
                    callback.onSuccess();
                    rePopulateAbstractCategories();
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
    public void updateAbstractEntity(@NonNull AbstractCategoryEntity entity,
                                     @NonNull String name,
                                     @NonNull String description,
                                     @NonNull OperationCompleteCallback callback) {
        executor.execute(() -> {
            try {
                if (CategoryEntity.class == entity.getClass()) {
                    // Get a copy of the current category. Must do this to trigger submitList() logic
                    CategoryEntity category = ((CategoryEntity) entity).toBuilder()
                            .name(name)
                            .description(description)
                            .build();
                    if (!repo.updateCategories(category)) {
                        callback.onFailure("Something went wrong");
                    } else {
                        callback.onSuccess();
                        rePopulateAbstractCategories();
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
        List<AbstractCategoryEntity> allCategories = uiCategoriesList.getValue();

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
}
