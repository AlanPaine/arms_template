import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '@viewPath/@lowercaseName_page.dart';

part 'route.g.dart';

/// 配置@name页面路由
@TypedGoRoute<@nameRoute>(path: '/@lowercaseName')
class @nameRoute extends GoRouteData {
  const @nameRoute();

  @override
  Widget build(BuildContext context, GoRouterState state){
    return const @namePage();
  }
}