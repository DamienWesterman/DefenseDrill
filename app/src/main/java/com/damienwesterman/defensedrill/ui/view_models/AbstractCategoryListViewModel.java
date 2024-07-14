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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.List;

// TODO doc comments
public interface AbstractCategoryListViewModel {
    LiveData<List<AbstractCategoryEntity>> getAbstractCategories();
    void populateAbstractCategories();
    void rePopulateAbstractCategories();
    void deleteAbstractCategory(AbstractCategoryEntity entity);
    void saveAbstractEntity(AbstractCategoryEntity entity, @NonNull CreateNewEntityCallback callback);
    void updateAbstractEntity(AbstractCategoryEntity entity);
    @Nullable
    AbstractCategoryEntity findById(long id);
}
