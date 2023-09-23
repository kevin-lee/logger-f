/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';

import {
  useVersions,
  useLatestVersion,
} from '@docusaurus/plugin-content-docs/client';

function Version() {
  const {siteConfig} = useDocusaurusContext();
  const versions = useVersions();
  const latestVersion = useLatestVersion();
  const currentVersion = versions.find((version) => version.name === 'current');
  const pastVersions = versions.filter(
    (version) => version !== latestVersion && version.name !== 'current',
  ).concat([
    {
      "name": "2.0.0-beta19",
      "label": "2.0.0-beta19",
    },
    {
      "name": "2.0.0-beta18",
      "label": "2.0.0-beta18",
    },
    {
      "name": "2.0.0-beta17",
      "label": "2.0.0-beta17",
    },
    {
      "name": "2.0.0-beta16",
      "label": "2.0.0-beta16",
    },
    {
      "name": "2.0.0-beta15",
      "label": "2.0.0-beta15",
    },
    {
      "name": "2.0.0-beta14",
      "label": "2.0.0-beta14",
    },
    {
      "name": "2.0.0-beta13",
      "label": "2.0.0-beta13",
    },
    {
      "name": "2.0.0-beta12",
      "label": "2.0.0-beta12",
    },
    {
      "name": "2.0.0-beta11",
      "label": "2.0.0-beta11",
    },
    {
      "name": "2.0.0-beta10",
      "label": "2.0.0-beta10",
    },
    {
      "name": "2.0.0-beta9",
      "label": "2.0.0-beta9",
    },
    {
      "name": "2.0.0-beta8",
      "label": "2.0.0-beta8",
    },
    {
      "name": "2.0.0-beta7",
      "label": "2.0.0-beta7",
    },
    {
      "name": "2.0.0-beta6",
      "label": "2.0.0-beta6",
    },
    {
      "name": "2.0.0-beta5",
      "label": "2.0.0-beta5",
    },
    {
      "name": "2.0.0-beta4",
      "label": "2.0.0-beta4",
    },
    {
      "name": "2.0.0-beta3",
      "label": "2.0.0-beta3",
    },
    {
      "name": "2.0.0-beta2",
      "label": "2.0.0-beta2",
    },
    {
      "name": "2.0.0-beta1",
      "label": "2.0.0-beta1",
    },
    {
      "name": "1.20.0",
      "label": "1.20.0",
    },
    {
      "name": "1.19.0",
      "label": "1.19.0",
    },
    {
      "name": "1.18.0",
      "label": "1.18.0",
    },
    {
      "name": "1.17.0",
      "label": "1.17.0",
    },
    {
      "name": "1.16.0",
      "label": "1.16.0",
    },
    {
      "name": "1.15.0",
      "label": "1.15.0",
    },
    {
      "name": "1.14.0",
      "label": "1.14.0",
    },
    {
      "name": "1.13.0",
      "label": "1.13.0",
    },
    {
      "name": "1.12.0",
      "label": "1.12.0",
    },
    {
      "name": "1.11.0",
      "label": "1.11.0",
    },
    {
      "name": "1.10.0",
      "label": "1.10.0",
    },
    {
      "name": "1.9.0",
      "label": "1.9.0",
    },
    {
      "name": "1.8.0",
      "label": "1.8.0",
    },
    {
      "name": "1.7.0",
      "label": "1.7.0",
    },
    {
      "name": "1.6.0",
      "label": "1.6.0",
    },
    {
      "name": "1.5.0",
      "label": "1.5.0",
    },
    {
      "name": "1.4.0",
      "label": "1.4.0",
    },
    {
      "name": "1.3.1",
      "label": "1.3.1",
    },
    {
      "name": "1.3.0",
      "label": "1.3.0",
    },
    {
      "name": "1.2.0",
      "label": "1.2.0",
    },
    {
      "name": "1.1.0",
      "label": "1.1.0",
    },
    {
      "name": "1.0.0",
      "label": "1.0.0",
    },
    {
      "name": "0.4.0",
      "label": "0.4.0",
    },
    {
      "name": "0.3.1",
      "label": "0.3.1",
    },
    {
      "name": "0.3.0",
      "label": "0.3.0",
    },
    {
      "name": "0.2.0",
      "label": "0.2.0",
    },
    {
      "name": "0.1.0",
      "label": "0.1.0",
    },
  ]).sort((a, b) => {
    // console.log(`a: ${JSON.stringify(a)}, b: ${JSON.stringify(b)}`);
    if (!a.name.includes(".") || !b.name.includes(".")) {
      if (a.name.includes("v")) {
        const aVersion = parseInt(a.name.substring(1).split(".")[0]);
        if (b.name.includes("v")) {
          const bVersion = parseInt(b.name.substring(1).split(".")[0]);
          return bVersion - aVersion;
        } else {
          const bVersion = parseInt(b.name.split(".")[0]);
          return bVersion - aVersion;
        }
      } else {
        const aVersion = parseInt(a.name.split(".")[0]);
        if (b.name.includes("v")) {
          const bVersion = parseInt(b.name.substring(1).split(".")[0]);
          return bVersion - aVersion;
        } else {
          const bVersion = parseInt(b.name.split(".")[0]);
          return bVersion - aVersion;
        }
      }
    }
    const [aMajor, aMinor, aPatchAndMore] = a.name.split(".");
    const [aPatch] = aPatchAndMore.split("-");
    const [bMajor, bMinor, bPatchAndMore] = b.name.split(".");
    const [bPatch] = bPatchAndMore.split("-");

    const a1 = parseInt(aMajor);
    const a2 = parseInt(aMinor);
    const a3 = parseInt(aPatch);

    const b1 = parseInt(bMajor);
    const b2 = parseInt(bMinor);
    const b3 = parseInt(bPatch);

    if (a1 > b1) {
      return -1;
    } else if (a1 === b1) {
      if (a2 > b2) {
        return -1;
      } else if (a2 === b2) {
        if (a3 > b3) {
          return -1;
        } else if (a3 === b3) {
          return 0;
        } else {
          return 1;
        }
      } else {
        return 1;
      }
    } else {
      return 1;
    }
  });
  console.log(JSON.stringify(pastVersions));
  // const stableVersion = pastVersions.shift();
  const stableVersion = currentVersion;
  const repoUrl = `https://github.com/${siteConfig.organizationName}/${siteConfig.projectName}`;

  const docLink = path => path ? <Link to={path}>Documentation</Link> : <span>&nbsp;</span>;

  const releaseLink = version => version.label !== "v1" ? <a href={`${repoUrl}/releases/tag/v${version.name}`}>Release Notes</a> : <span>&nbsp;</span>;

  const spaces = howMany => <span dangerouslySetInnerHTML={{__html: "&nbsp;".repeat(howMany)}} />;

  return (
    <Layout
      title="Versions"
      description="Effectie Versions page listing all documented site versions">
      <main className="container margin-vert--lg">
        <h1>Effectie documentation versions</h1>

        {stableVersion && (
          <div className="margin-bottom--lg">
            <h3 id="next">Current version (Stable)</h3>
            <p>
              Here you can find the documentation for current released version.
            </p>
            <table>
              <tbody>
              <tr>
                <th>{stableVersion.label}</th>
                <td>
                  <Link to={stableVersion.path}>Documentation</Link>
                </td>
                <td>
                  <a href={`${repoUrl}/releases/tag/v${stableVersion.label}`}>
                    Release Notes
                  </a>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        )}
        {/*
        <div className="margin-bottom--lg">
          <h3 id="latest">Next version (Unreleased)</h3>
          <p>
            Here you can find the documentation for work-in-process unreleased
            version.
          </p>
          <table>
            <tbody>
            <tr>
              <th>{latestVersion.label}</th>
              <td>
                <Link to={latestVersion.path}>Documentation</Link>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
        */}

        {pastVersions.length > 0 && (
          <div className="margin-bottom--lg">
            <h3 id="archive">Past versions (Not maintained anymore)</h3>
            <p>
              Here you can find documentation for previous versions of
              Effectie.
            </p>
            <table>
              <tbody>
              {pastVersions.map((version) => (
                <tr key={version.name}>
                  <th>{version.label}</th>
                  <td>
                    {docLink(version.path)}
                  </td>
                  <td>
                    {releaseLink(version)}
                  </td>
                </tr>
              ))}
              </tbody>
            </table>
          </div>
        )}
        {
        //   <div className="margin-bottom--lg">
        //   <h3 id="archive">Past versions without documentation (Not maintained anymore)</h3>
        //   <p>
        //     Here you can find documentation for previous versions of
        //     Effectie.
        //   </p>
        //   <table>
        //     <tbody>
        //       <tr key="1.15.0">
        //         <th>1.15.0</th>
        //         <td>
        //           {spaces(27)}
        //         </td>
        //         <td>
        //           <a href={`${repoUrl}/releases/tag/v1.15.0`}>
        //             Release Notes
        //           </a>
        //         </td>
        //       </tr>
        //     </tbody>
        //   </table>
        // </div>
        }

      </main>
    </Layout>
  );
}

export default Version;