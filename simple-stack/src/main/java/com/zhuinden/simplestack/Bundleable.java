/*
 * Copyright 2017 Gabor Varadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhuinden.simplestack;

import com.zhuinden.statebundle.StateBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Specifies the ability to persist the state of the object to a StateBundle.
 *
 * This is used by the root custom view, and also to persist the state of scoped services.
 *
 * The {@link Backstack} is also marked as {@link Bundleable}.
 */
public interface Bundleable {
    @Nonnull
    StateBundle toBundle();

    void fromBundle(@Nullable StateBundle bundle);
}
