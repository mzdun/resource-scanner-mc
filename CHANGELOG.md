# Changelog

All notable changes to this project will be documented in this file. See [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) and [COMMITS.md](COMMITS.md) for commit guidelines.

## [0.5.0](https://github.com/mzdun/resource-scanner-mc/compare/v0.4.1...v0.5.0) (2024-10-09)

### New Features

- locate the thing I'm looking at ([e6d0658](https://github.com/mzdun/resource-scanner-mc/commit/e6d0658f69e933c0dfa15b8df371fdaa4b22c5f7))
- clumping echoes in nuggets ([1dfd23d](https://github.com/mzdun/resource-scanner-mc/commit/1dfd23d2415298e70c576081ab541a3fcb823bf7)), closes [#23](https://github.com/mzdun/resource-scanner-mc/issues/23), fixes [#22](https://github.com/mzdun/resource-scanner-mc/issues/22)
- adding new level of indirection ([1a8b096](https://github.com/mzdun/resource-scanner-mc/commit/1a8b096883388ec7a44ec65d36defb7baf1aafa9))

### Chores

- use :fabric:1.21 as base for other versions ([666e979](https://github.com/mzdun/resource-scanner-mc/commit/666e97992627fe15aaba36e49fefafd9c1ff63fc))

### Code Refactoring

- merging `BlockEcho` and `EchoState` ([5b6f2fd](https://github.com/mzdun/resource-scanner-mc/commit/5b6f2fd4a6c84de7b75d64eb0d70902aebcc48ac))
- s/GraphicContext/ShimmerConsumer/ ([fa0b40a](https://github.com/mzdun/resource-scanner-mc/commit/fa0b40a770236f6f443a8d7cbc5f0c1ad3eec4ad))

## [0.4.1](https://github.com/mzdun/resource-scanner-mc/compare/v0.4.0...v0.4.1) (2024-10-07)

### Bug Fixes

- crash during active scan with no more world ([39e3be0](https://github.com/mzdun/resource-scanner-mc/commit/39e3be0d032ce7bedc0595e3dcaf15998ad1a468)), fixes [#21](https://github.com/mzdun/resource-scanner-mc/issues/21)
- only one setting at a time is saved ([24ac468](https://github.com/mzdun/resource-scanner-mc/commit/24ac468a0d78fdb21a77a5507eed52f2c16f60bd)), fixes [#20](https://github.com/mzdun/resource-scanner-mc/issues/20)

## [0.4.0](https://github.com/mzdun/resource-scanner-mc/compare/v0.3.2...v0.4.0) (2024-10-07)

### New Features

- color echoes based on `c:ores` subclasses ([1688382](https://github.com/mzdun/resource-scanner-mc/commit/1688382752b35126e709cb1ce8ee1a931f10f7d4))
- add ARGB32 color to each block ([1920f08](https://github.com/mzdun/resource-scanner-mc/commit/1920f08f004aca08a4523effca4884ff44b49e4e))
- drop maximum in favor of lifetime of echoes ([50cda36](https://github.com/mzdun/resource-scanner-mc/commit/50cda3655441b9d3c583b6a7f85ee1b7d9b6130d))
- add echoes lifetime ([2626d87](https://github.com/mzdun/resource-scanner-mc/commit/2626d879763f9e5121115790d77536ea6aa63a49))
- attach sonar drawing to the mod ([ce72ea5](https://github.com/mzdun/resource-scanner-mc/commit/ce72ea5769c25d28a96c78280bbf49a7cac74284))
- draw echoes even if no shimmers are present ([919de95](https://github.com/mzdun/resource-scanner-mc/commit/919de95143a1efd13c281a9e315e32aa97f9d7e9))
- get the sound sample from Amane:OG ([4f9f0bc](https://github.com/mzdun/resource-scanner-mc/commit/4f9f0bc459095e5867eee676593289e21623c59e))
- add activation sound event ([3cc1d91](https://github.com/mzdun/resource-scanner-mc/commit/3cc1d91696fad1d0d01e6e199a3abb1d6dbf8181))
- attach sonar animation to the mod ([4ace428](https://github.com/mzdun/resource-scanner-mc/commit/4ace428b313a7ea03e8deee62e3280b633df7d03))
- animate the shimmers and echoes ([37310e0](https://github.com/mzdun/resource-scanner-mc/commit/37310e0dc1d9d57ee625f7461629a0294f77420f))
- introduce animation subsystem ([4ae3f26](https://github.com/mzdun/resource-scanner-mc/commit/4ae3f26f99e48ecffd3b4490527ab2d0b72e078e))

### Bug Fixes

- mined echoes remain on screen ([7e2611f](https://github.com/mzdun/resource-scanner-mc/commit/7e2611f7ad29069d94a6cadd302e512d93305218)), fixes [#18](https://github.com/mzdun/resource-scanner-mc/issues/18)
- use better name for slice pacer ([ad1e442](https://github.com/mzdun/resource-scanner-mc/commit/ad1e44235849b78af33a231ebe7d7ab4395916b5))
- **docs**: update the README.md with scan screenshots ([fa7dfe4](https://github.com/mzdun/resource-scanner-mc/commit/fa7dfe4477a90bca7b33220fc4326803ee1d5ee6))

## [0.3.2](https://github.com/mzdun/resource-scanner-mc/compare/v0.3.1...v0.3.2) (2024-10-01)

### Bug Fixes

- align Fabric-facing license with GitHub one ([9f81b24](https://github.com/mzdun/resource-scanner-mc/commit/9f81b2495990cab9447107fa2e58a48d2ec7c54f))

## [0.3.1](https://github.com/mzdun/resource-scanner-mc/compare/v0.3.0...v0.3.1) (2024-10-01)

### Bug Fixes

- decide the environment in the mod manifest ([436199b](https://github.com/mzdun/resource-scanner-mc/commit/436199bbd4f2173bf5f35cbe9c55171a7fa587d9))

## [0.3.0](https://github.com/mzdun/resource-scanner-mc/compare/v0.2.0...v0.3.0) (2024-10-01)

### New Features

- add scrollbar to inventory widget ([cb03d1f](https://github.com/mzdun/resource-scanner-mc/commit/cb03d1f6c29de7b529e178ec408ecb9242261428))
- expand inventory to two rows at the bottom ([9f79e9c](https://github.com/mzdun/resource-scanner-mc/commit/9f79e9c74882b9a4878220b97e587e18fa81e4a8))
- add modmenu settings screens ([47bc149](https://github.com/mzdun/resource-scanner-mc/commit/47bc149a865355d3827239e62516cb6e21443483))

### Bug Fixes

- cut down initial ore set to coal ([36cd931](https://github.com/mzdun/resource-scanner-mc/commit/36cd931185baeba60720891563a97f95ab90a955))
- **docs**: show first screenshots ([3b8422e](https://github.com/mzdun/resource-scanner-mc/commit/3b8422e1220d727054c6dd1a1969525a5b63db98))

## [0.2.0](https://github.com/mzdun/resource-scanner-mc/compare/v0.2.0-alpha...v0.2.0) (2024-09-27)

### Bug Fixes

- **docs**: update the message type list ([607801e](https://github.com/mzdun/resource-scanner-mc/commit/607801edb9dd63dfe0ad9627d5209ea3bcea57a2))

### Chores

- add the license notification to source code ([d1b37e7](https://github.com/mzdun/resource-scanner-mc/commit/d1b37e7e88ce981d027410b24884a59e7e6b9a2f))
- clean up code; make use of `var` ([fc4768b](https://github.com/mzdun/resource-scanner-mc/commit/fc4768b488f24e61275cec64e9f293888223bdd3))

### Continuous Integration

- add version override for `github release` ([98983f0](https://github.com/mzdun/resource-scanner-mc/commit/98983f0fca2437d0708c7645e40f591e3373d6de))
- reintroduce an empty line after H2 ([11b8acb](https://github.com/mzdun/resource-scanner-mc/commit/11b8acb4d9ed67b986d9d0fb057b9e34c1abebf4))
- reorganize the CI scripts ([a6475b1](https://github.com/mzdun/resource-scanner-mc/commit/a6475b137e83536a25bd38286a897b13f41aa302))
- fix repository name lookup ([c1ca40b](https://github.com/mzdun/resource-scanner-mc/commit/c1ca40b6d3d4075056ce8e157aea84161c81ff06))
- fix the Publish running on non-release builds ([bccd12b](https://github.com/mzdun/resource-scanner-mc/commit/bccd12b7c3e6b35a998fbacfb0724b725d45a2f6))
- rename the jobs ([8393236](https://github.com/mzdun/resource-scanner-mc/commit/83932363277665aa7d11a27122d07b253250c5c8))
- allow releases from annotated tags ([9eac5be](https://github.com/mzdun/resource-scanner-mc/commit/9eac5bee769dbb3cf3e46f24accc812d57c1bddc))

### Code Style

- apply linter changes ([c518be2](https://github.com/mzdun/resource-scanner-mc/commit/c518be2a8e3e72e05d3689a86f6f5e2c7dd0871a))
