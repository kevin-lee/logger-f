"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[42],{351:(e,t,n)=>{n.r(t),n.d(t,{default:()=>i});var a=n(7294),l=n(2263),r=n(9960),s=n(7110),m=n(4104);const i=function(){const{siteConfig:e}=(0,l.Z)(),t=(0,m.gB)(),n=(0,m.yW)(),i=t.find((e=>"current"===e.name)),c=t.filter((e=>e!==n&&"current"!==e.name)).concat([{name:"2.0.0-beta8",label:"2.0.0-beta8"},{name:"2.0.0-beta7",label:"2.0.0-beta7"},{name:"2.0.0-beta6",label:"2.0.0-beta6"},{name:"2.0.0-beta5",label:"2.0.0-beta5"},{name:"2.0.0-beta4",label:"2.0.0-beta4"},{name:"2.0.0-beta3",label:"2.0.0-beta3"},{name:"2.0.0-beta2",label:"2.0.0-beta2"},{name:"2.0.0-beta1",label:"2.0.0-beta1"},{name:"1.20.0",label:"1.20.0"}]).sort(((e,t)=>{if(!e.name.includes(".")||!t.name.includes(".")){if(e.name.includes("v")){const n=parseInt(e.name.substring(1).split(".")[0]);if(t.name.includes("v")){return parseInt(t.name.substring(1).split(".")[0])-n}return parseInt(t.name.split(".")[0])-n}{const n=parseInt(e.name.split(".")[0]);if(t.name.includes("v")){return parseInt(t.name.substring(1).split(".")[0])-n}return parseInt(t.name.split(".")[0])-n}}const[n,a,l]=e.name.split("."),[r]=l.split("-"),[s,m,i]=t.name.split("."),[c]=i.split("-"),o=parseInt(n),u=parseInt(a),b=parseInt(r),p=parseInt(s),E=parseInt(m),d=parseInt(c);return o>p?-1:o===p?u>E?-1:u===E?b>d?-1:b===d?0:1:1:1}));console.log(JSON.stringify(c));const o=i,u=`https://github.com/${e.organizationName}/${e.projectName}`;return a.createElement(s.Z,{title:"Versions",description:"Effectie Versions page listing all documented site versions"},a.createElement("main",{className:"container margin-vert--lg"},a.createElement("h1",null,"Effectie documentation versions"),o&&a.createElement("div",{className:"margin-bottom--lg"},a.createElement("h3",{id:"next"},"Current version (Stable)"),a.createElement("p",null,"Here you can find the documentation for current released version."),a.createElement("table",null,a.createElement("tbody",null,a.createElement("tr",null,a.createElement("th",null,o.label),a.createElement("td",null,a.createElement(r.Z,{to:o.path},"Documentation")),a.createElement("td",null,a.createElement("a",{href:`${u}/releases/tag/v${o.label}`},"Release Notes")))))),c.length>0&&a.createElement("div",{className:"margin-bottom--lg"},a.createElement("h3",{id:"archive"},"Past versions (Not maintained anymore)"),a.createElement("p",null,"Here you can find documentation for previous versions of Effectie."),a.createElement("table",null,a.createElement("tbody",null,c.map((e=>{return a.createElement("tr",{key:e.name},a.createElement("th",null,e.label),a.createElement("td",null,(t=e.path)?a.createElement(r.Z,{to:t},"Documentation"):a.createElement("span",null,"\xa0")),a.createElement("td",null,(e=>"v1"!==e.label?a.createElement("a",{href:`${u}/releases/tag/v${e.name}`},"Release Notes"):a.createElement("span",null,"\xa0"))(e)));var t}))))),a.createElement("div",{className:"margin-bottom--lg"},a.createElement("h3",{id:"archive"},"Past versions without documentation (Not maintained anymore)"),a.createElement("p",null,"Here you can find documentation for previous versions of Effectie."),a.createElement("table",null,a.createElement("tbody",null,a.createElement("tr",{key:"1.15.0"},a.createElement("th",null,"1.15.0"),a.createElement("td",null,(b=27,a.createElement("span",{dangerouslySetInnerHTML:{__html:"&nbsp;".repeat(b)}}))),a.createElement("td",null,a.createElement("a",{href:`${u}/releases/tag/v1.15.0`},"Release Notes"))))))));var b}}}]);