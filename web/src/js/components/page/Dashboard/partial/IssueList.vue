<style scoped>
    .clear {
        clear: both;
    }

    .issue-list {
        width: 100%;
        height: 100%;
        overflow: auto;
    }

    .issue-list-container {
        width: 90%;
        margin: 0 auto;
    }

    .new-issue-button {
        height: 32px;
        float: right;
        border: 1px solid #000000;
        border-radius: 4px;
        background-color: antiquewhite;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 8px;
        margin-left: 4px;
    }

    .new-issue-button a {
        color: #000000;
        text-decoration: none;
    }

    .reload-button {
        height: 32px;
        float: right;
        border: 1px solid #000000;
        border-radius: 4px;
        background-color: antiquewhite;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 8px;
    }

    table {
        width: 100%;
    }

    td {
        padding: 20px 4px;
    }

    .status-cell {
        width: 80px;
        font-size: small;
        text-align: center;
        vertical-align: middle;
    }

    .close-cell {
        width: 80px;
        font-size: small;
        text-align: center;
        vertical-align: middle;
    }

    .status-button-group > div:first-child{
        border-top: solid 1px #000000;
        border-radius: 4px 4px 0 0;
    }
    .status-button-group > div{
        cursor: pointer;
        border-bottom: solid 1px #000000;
        border-left: solid 1px #000000;
        border-right: solid 1px #000000;
        background-color: antiquewhite;
        height: 32px;
        font-weight: bold;
        display: flex;
        justify-content: center;
        align-items: center;
    }
    .status-button-group > div:last-child{
        border-radius: 0 0 4px 4px;
    }

    .status-button-group > div.current-status {
        background-color: #b38324
    }

    .close-button {
        height: 32px;
        background-color: antiquewhite;
        cursor: pointer;
        border: solid 1px;
        background-color: antiquewhite;
        font-weight: bold;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 4px;
    }

    h1 {
        width: 100%;
        border-bottom: dashed 2px;
    }

    tr td {
        border-bottom: solid 1px gray;
    }

    .list-complete-item {
        transition: all 0.5s;
    }
    .list-complete-leave-to, .list-complete-leave-active {
        opacity: 0;
    }

    a {
        color: #1482b3;
    }

    .logo {
        height: 100%;
        font-size: 100px;
        font-weight: bold;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .synchronizing {
        color: #666666;
    }

    .issue-control {
        border-bottom: solid 1px #444444;
        padding-bottom: 8px;
    }

</style>
<template>
    <div class="issue-list">
        <div v-if="isLoading" class="issue-list-container loader">
            <div class="spinner-loader"></div>
        </div>
        <div v-else class="issue-list-container">
            <div v-if="selectedProject == null" class="logo">
                BackLogger
            </div>
            <div v-else>
                <h1>
                    自分が担当している
                    <a :href="`https://${spaceName}.backlog.jp/projects/${selectedProject.key}`" target="_blank">
                    {{selectedProject.name}}
                    </a>
                    の未完了課題
                </h1>
                <div class="issue-control">
                    <div class="new-issue-button">
                        <a :href="`https://${spaceName}.backlog.jp/add/${selectedProject.key}`" target="_blank">
                            [+] 新しい課題を登録
                        </a>
                    </div>
                    <div class="reload-button" @click="loadAllIssues">課題をリロード</div>
                    <div class="clear"></div>
                </div>
                <table>
                    <transition-group name="list-complete" tag="tbody">
                        <tr v-for="i in issues" :key="i.id" class="list-complete-item">
                            <td class="summary-cell">
                                <a :href="`https://${spaceName}.backlog.jp/view/${i.key}`" target="_blank">{{i.summary}}</a>
                            </td>
                            <td class="status-cell">
                                <div class="status-button-group" :class="{'synchronizing': i.synchronizing}">
                                    <div :class="{'current-status': i.status == 'untreated'}"
                                         @click="makeIssueStatusAsUntreated(i.id)">
                                        未対応
                                    </div>
                                    <div :class="{'current-status': i.status == 'processing'}"
                                         @click="makeIssueStatusAsProcessing(i.id)">
                                        処理中
                                    </div>
                                    <div :class="{'current-status': i.status == 'processed'}"
                                         @click="makeIssueStatusAsProcessed(i.id)">
                                        処理済み
                                    </div>
                                </div>
                            </td>
                            <td class="close-cell">
                                <div class="close-button" :class="{'synchronizing': i.synchronizing}" @click="closeIssue(i.id)">
                                    完了
                                </div>
                            </td>
                        </tr>
                    </transition-group>
                </table>
            </div>
        </div>
    </div>
</template>
<script>
    import {
        LoadIssuesCommand,
        MakeIssueStatusAsUntreatedCommand,
        MakeIssueStatusAsProcessingCommand,
        MakeIssueStatusAsProcessedCommand,
        CloseIssueCommand,
        IssueEvents,
        IssueQuery,
        SettingQuery
    } from '../../../../../../../scala/target/scala-2.12/backlogger-opt'

    import base from '../../base'

    export default {
        mixins: [base],

        props: ['selectedProject'],

        beforeMount() {
            const query = new IssueQuery;
            this.subscriptions.push(
                IssueEvents.loaded.subscribe(() => {
                    this.issues = query.allOf(this.selectedProject.id)
                    this.isLoading = false;
                })
            );
            this.subscriptions.push(
                IssueEvents.repositoryChanged.subscribe(() => {
                    this.issues = query.allOf(this.selectedProject.id)
                })
            );
        },

        watch: {
            selectedProject(){
                this.loadAllIssues();
            }
        },

        data(){
            const q = new SettingQuery
            return {
                issues: [],
                isLoading: false,
                spaceName: q.spaceName()
            }
        },

        methods: {
            loadAllIssues(){
                this.isLoading = true;
                const command = new LoadIssuesCommand();
                command.projectId = this.selectedProject.id;
                command.execute();
            },

            makeIssueStatusAsUntreated(issueId){
                const command = new MakeIssueStatusAsUntreatedCommand;
                command.projectId = this.selectedProject.id;
                command.issueId = issueId;
                command.execute();
            },

            makeIssueStatusAsProcessing(issueId){
                const command = new MakeIssueStatusAsProcessingCommand;
                command.projectId = this.selectedProject.id;
                command.issueId = issueId;
                command.execute();
            },

            makeIssueStatusAsProcessed(issueId){
                const command = new MakeIssueStatusAsProcessedCommand;
                command.projectId = this.selectedProject.id;
                command.issueId = issueId;
                command.execute();
            },

            closeIssue(issueId){
                const command = new CloseIssueCommand;
                command.projectId = this.selectedProject.id;
                command.issueId = issueId;
                command.execute();
            },
        }
    }
</script>
