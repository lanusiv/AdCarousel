# AdCarousel - An easy way to implement images and videos play

## Feasures
- showing images and videos all all together in one view, simply by setting one data source
- auto request and download images and videos for cache
- support for several type of page transformation for viewpager
- auto play
- support for d-pad navigation

## How to use?
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

        AdView adView = (AdView) findViewById(R.id.adView);
        adView.initAdView(this);
        adView.loadAdList(getData());
    }
```

## Questions?
[ilanusiv@gmail.com](mailto:ilanusiv@gmail.com)

## License

    Copyright 2015-2016 Leray Better

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
