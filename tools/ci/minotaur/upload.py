# Copyright (c) 2024 Marcin Zdun
# This code is licensed under MIT license (see LICENSE for details)

from typing import Dict, List, NamedTuple, Optional, Set, cast

from ..common.gradle import GradleProject
from ..common.project import Project
from ..common.packages import buildRegex, getPackages
from .endpoints import CreateVersion


def _versionType(project: Project):
    stability = project.version.stability.value
    if stability == "-alpha" or stability[:7] == "-alpha.":
        return "alpha"
    if stability == "-beta" or stability[:7] == "-beta.":
        return "beta"
    return "release"


class Dep(NamedTuple):
    id: str
    type: str

    def json(self) -> dict:
        return {
            "project_id": self.id,
            "dependency_type": self.type
        }


class Loader(NamedTuple):
    name: str
    suffix: str
    deps: List[Dep]

    def depJson(self):
        return [dep.json() for dep in self.deps]


class Archive(NamedTuple):
    dirname: str
    basename: str
    minecraftVersion: str
    loaders: List[Loader]
    project: Project
    otherDeps: List[Dep]

    @property
    def properties(self):
        return cast(GradleProject, self.project).properties

    def endpoint(self, project_id: str, changelog: Optional[str]):
        suffixes = ''.join([f'-{loader.suffix}' for loader in self.loaders])
        meta_suffixes = ''.join([f'-{loader.name}' for loader in self.loaders])
        mc = f'{self.minecraftVersion}{suffixes}'
        meta = f'{self.minecraftVersion}{meta_suffixes}'
        mod_name = self.properties.mod_name
        name = f"{mod_name} {self.project.version} for {mc}"
        return CreateVersion(
            dirname=self.dirname,
            basename=self.basename,
            name=name,
            project_id=project_id,
            version_number=f"{self.project.version}+{meta}",
            dependencies=self._mergedDeps(),
            game_versions=[self.minecraftVersion],
            version_type=_versionType(self.project),
            loaders=[loader.name for loader in self.loaders],
            featured=True,
            changelog=changelog,
        )

    def _mergedDeps(self):
        ids: Set[str] = set()
        results: List[dict] = []
        for loader in self.loaders:
            for dep in loader.depJson():
                project_id = dep["project_id"]
                if project_id in ids:
                    continue
                results.append(dep)
                ids.add(project_id)
            for dep in [d.json() for d in self.otherDeps]:
                project_id = dep["project_id"]
                if project_id in ids:
                    continue
                results.append(dep)
                ids.add(project_id)
        return results


def _loadersFromIds(loaderIds: List[str], knownLoaders: Dict[str, Loader]):
    loaders: List[Loader] = []
    for id in loaderIds:
        loader = knownLoaders.get(id)
        if loader is not None:
            loaders.append(loader)
    return loaders


def enumArchives(src: str, project: Project, knownLoaders: Dict[str, Loader], *otherDeps: Dep):
    matcher = buildRegex(project)
    src, names = getPackages(src, matcher)

    uploads: List[Archive] = []
    for name in names:
        mcVersion, *loaderIds = matcher.match(name).group(1).split('-')
        loaders = _loadersFromIds(loaderIds, knownLoaders)
        if len(loaders) == 0:
            continue
        uploads.append(Archive(src, name, mcVersion,
                       loaders, project, otherDeps))

    return uploads
