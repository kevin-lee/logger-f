"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[42],{351:(e,a,t)=>{t.r(a),t.d(a,{default:()=>s});var l=t(7294),n=t(2263),m=t(9960),r=t(7110),b=t(4104);const s=function(){const{siteConfig:e}=(0,n.Z)(),a=(0,b.gB)(),t=(0,b.yW)(),s=a.find((e=>"current"===e.name)),i=a.filter((e=>e!==t&&"current"!==e.name)).concat([{name:"2.0.0-beta16",label:"2.0.0-beta16"},{name:"2.0.0-beta15",label:"2.0.0-beta15"},{name:"2.0.0-beta14",label:"2.0.0-beta14"},{name:"2.0.0-beta13",label:"2.0.0-beta13"},{name:"2.0.0-beta12",label:"2.0.0-beta12"},{name:"2.0.0-beta11",label:"2.0.0-beta11"},{name:"2.0.0-beta10",label:"2.0.0-beta10"},{name:"2.0.0-beta9",label:"2.0.0-beta9"},{name:"2.0.0-beta8",label:"2.0.0-beta8"},{name:"2.0.0-beta7",label:"2.0.0-beta7"},{name:"2.0.0-beta6",label:"2.0.0-beta6"},{name:"2.0.0-beta5",label:"2.0.0-beta5"},{name:"2.0.0-beta4",label:"2.0.0-beta4"},{name:"2.0.0-beta3",label:"2.0.0-beta3"},{name:"2.0.0-beta2",label:"2.0.0-beta2"},{name:"2.0.0-beta1",label:"2.0.0-beta1"},{name:"1.20.0",label:"1.20.0"},{name:"1.19.0",label:"1.19.0"},{name:"1.18.0",label:"1.18.0"},{name:"1.17.0",label:"1.17.0"},{name:"1.16.0",label:"1.16.0"},{name:"1.15.0",label:"1.15.0"},{name:"1.14.0",label:"1.14.0"},{name:"1.13.0",label:"1.13.0"},{name:"1.12.0",label:"1.12.0"},{name:"1.11.0",label:"1.11.0"},{name:"1.10.0",label:"1.10.0"},{name:"1.9.0",label:"1.9.0"},{name:"1.8.0",label:"1.8.0"},{name:"1.7.0",label:"1.7.0"},{name:"1.6.0",label:"1.6.0"},{name:"1.5.0",label:"1.5.0"},{name:"1.4.0",label:"1.4.0"},{name:"1.3.1",label:"1.3.1"},{name:"1.3.0",label:"1.3.0"},{name:"1.2.0",label:"1.2.0"},{name:"1.1.0",label:"1.1.0"},{name:"1.0.0",label:"1.0.0"},{name:"0.4.0",label:"0.4.0"},{name:"0.3.1",label:"0.3.1"},{name:"0.3.0",label:"0.3.0"},{name:"0.2.0",label:"0.2.0"},{name:"0.1.0",label:"0.1.0"}]).sort(((e,a)=>{if(!e.name.includes(".")||!a.name.includes(".")){if(e.name.includes("v")){const t=parseInt(e.name.substring(1).split(".")[0]);if(a.name.includes("v")){return parseInt(a.name.substring(1).split(".")[0])-t}return parseInt(a.name.split(".")[0])-t}{const t=parseInt(e.name.split(".")[0]);if(a.name.includes("v")){return parseInt(a.name.substring(1).split(".")[0])-t}return parseInt(a.name.split(".")[0])-t}}const[t,l,n]=e.name.split("."),[m]=n.split("-"),[r,b,s]=a.name.split("."),[i]=s.split("-"),c=parseInt(t),o=parseInt(l),u=parseInt(m),p=parseInt(r),E=parseInt(b),d=parseInt(i);return c>p?-1:c===p?o>E?-1:o===E?u>d?-1:u===d?0:1:1:1}));console.log(JSON.stringify(i));const c=s,o=`https://github.com/${e.organizationName}/${e.projectName}`;return l.createElement(r.Z,{title:"Versions",description:"Effectie Versions page listing all documented site versions"},l.createElement("main",{className:"container margin-vert--lg"},l.createElement("h1",null,"Effectie documentation versions"),c&&l.createElement("div",{className:"margin-bottom--lg"},l.createElement("h3",{id:"next"},"Current version (Stable)"),l.createElement("p",null,"Here you can find the documentation for current released version."),l.createElement("table",null,l.createElement("tbody",null,l.createElement("tr",null,l.createElement("th",null,c.label),l.createElement("td",null,l.createElement(m.Z,{to:c.path},"Documentation")),l.createElement("td",null,l.createElement("a",{href:`${o}/releases/tag/v${c.label}`},"Release Notes")))))),i.length>0&&l.createElement("div",{className:"margin-bottom--lg"},l.createElement("h3",{id:"archive"},"Past versions (Not maintained anymore)"),l.createElement("p",null,"Here you can find documentation for previous versions of Effectie."),l.createElement("table",null,l.createElement("tbody",null,i.map((e=>{return l.createElement("tr",{key:e.name},l.createElement("th",null,e.label),l.createElement("td",null,(a=e.path)?l.createElement(m.Z,{to:a},"Documentation"):l.createElement("span",null,"\xa0")),l.createElement("td",null,(e=>"v1"!==e.label?l.createElement("a",{href:`${o}/releases/tag/v${e.name}`},"Release Notes"):l.createElement("span",null,"\xa0"))(e)));var a})))))))}}}]);