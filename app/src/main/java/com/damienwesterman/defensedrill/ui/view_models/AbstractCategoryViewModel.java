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

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.damienwesterman.defensedrill.data.local.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.util.List;

/**
 * Abstract superclass for view models for any {@link AbstractCategoryEntity} subclass.
 * <br><br>
 * This view model is geared towards managing a list of all the AbstractCategoryEntities in the
 * database, with CRUD functionality.
 */
public abstract class AbstractCategoryViewModel extends AndroidViewModel {
    public AbstractCategoryViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Get the LiveData list of AbstractCategoryEntity objects to observe.
     *
     * @return  LiveData object.
     */
    public abstract LiveData<List<AbstractCategoryEntity>> getAbstractCategories();

    /**
     * Populate our list of abstract categories if it has not already been done.
     */
    public abstract void populateAbstractCategories();

    /**
     * Force re-load the abstract categories from the database, even if they are already loaded.
     */
    public abstract void rePopulateAbstractCategories();

    /**
     * Delete an AbstractCategoryEntity from the database.
     *
     * @param entity AbstractCategoryEntity to delete.
     */
    public abstract void deleteAbstractCategory(AbstractCategoryEntity entity);

    /**
     * Attempt to add a new AbstractCategoryEntity to the database.
     * <br><br>
     * Which database will depend on the implementation of this class.
     *
     * @param name          Name of the abstract category.
     * @param description   Description of the abstract category.
     * @param callback      Callback to call when the insert is finished.
     */
    public abstract void saveAbstractEntity(String name, String description, @NonNull OperationCompleteCallback callback);

    /**
     * Attempt to update an AbstractCategoryEntity in the database.
     * <br><br>
     * Which database will depend on the implementation of this class.
     *
     * @param entity    AbstractCategoryEntity object to attempt to update.
     * @param callback  Callback to call when the update is finished.
     */
    public abstract void updateAbstractEntity(AbstractCategoryEntity entity, @NonNull OperationCompleteCallback callback);

    /**
     * Retrieve a single AbstractCategoryEntity by the associated database ID.
     *
     * @param id    ID of the desired abstract category.
     * @return      The AbstractCategoryEntity that maps to the given ID, or null if is doesn't
     *              exist.
     */
    @Nullable
    public abstract AbstractCategoryEntity findById(long id);
}
